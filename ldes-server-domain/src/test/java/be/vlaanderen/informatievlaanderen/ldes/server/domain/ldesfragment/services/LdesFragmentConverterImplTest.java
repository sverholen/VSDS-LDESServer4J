package be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesfragment.services;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.config.LdesConfig;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.converter.PrefixAdder;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.converter.PrefixAdderImpl;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesfragment.entities.LdesFragment;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesfragment.valueobjects.FragmentInfo;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesfragment.valueobjects.TreeRelation;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesmember.entities.LdesMember;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesmember.repository.LdesMemberRepository;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static be.vlaanderen.informatievlaanderen.ldes.server.domain.constants.RdfConstants.*;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LdesFragmentConverterImplTest {

	private static final String HOST_NAME = "http://localhost:8080";
	private static final String VIEW_NAME = "view";
	private static final String FRAGMENTATION_VALUE_1 = "2020-12-28T09:36:09.72Z";
	private static final String FRAGMENT_ID = HOST_NAME + "/" + VIEW_NAME;
	private static final String TIMESTAMP_PATH = "http://www.w3.org/ns/prov#generatedAtTime";
	public static final String DATE_TIME_TYPE = "http://www.w3.org/2001/XMLSchema#dateTime";

	private final LdesMemberRepository ldesMemberRepository = mock(LdesMemberRepository.class);
	private final PrefixAdder prefixAdder = new PrefixAdderImpl();
	private LdesFragmentConverterImpl ldesFragmentConverter;

	@BeforeEach
	void setUp() {
		LdesConfig ldesConfig = new LdesConfig();
		ldesConfig.setCollectionName("mobility-hindrances");
		ldesConfig.setHostName("http://localhost:8080");
		ldesConfig.setShape("https://private-api.gipod.test-vlaanderen.be/api/v1/ldes/mobility-hindrances/shape");
		ldesConfig.setMemberType("https://data.vlaanderen.be/ns/mobiliteit#Mobiliteitshinder");
		ldesConfig.setTimestampPath("http://www.w3.org/ns/prov#generatedAtTime");
		ldesConfig.setVersionOf("http://purl.org/dc/terms/isVersionOf");
		ldesFragmentConverter = new LdesFragmentConverterImpl(ldesMemberRepository,
				prefixAdder, ldesConfig);
	}

	@Test
	@DisplayName("Verify correct conversion of an LdesFragment without Members ")
	void when_LdesFragmentHasNoMembers_ModelHasOneStatement() {
		LdesFragment ldesFragment = new LdesFragment(
				new FragmentInfo(VIEW_NAME, List.of()));

		Model model = ldesFragmentConverter.toModel(ldesFragment);

		assertEquals(1, getNumberOfStatements(model));
		verifyTreeNodeStatement(model);
	}

	@Test
	void when_LdesFragmentHasMembers_ModelHasTreeNodeStatementAndEventStreamStatementsAndMemberStatements() {
		Model ldesMemberModel = RDFParserBuilder.create().fromString(
				"""
						<http://localhost:8080/mobility-hindrances> <https://w3id.org/tree#member>
						<https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10228622/165>
						.""")
				.lang(Lang.NQUADS).toModel();
		LdesMember ldesMember = new LdesMember("some_id", ldesMemberModel);
		LdesFragment ldesFragment = new LdesFragment(
				new FragmentInfo(VIEW_NAME,
						List.of()));
		ldesFragment.addMember("https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10228622/165");
		ldesFragment.addRelation(new TreeRelation("path", "/node", "value",
				DATE_TIME_TYPE, "relation"));
		when(ldesMemberRepository.getLdesMembersByIds(
				List.of("https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10228622/165")))
				.thenReturn(Stream.of(ldesMember));

		Model model = ldesFragmentConverter.toModel(ldesFragment);

		assertEquals(8, getNumberOfStatements(model));
		verifyTreeNodeStatement(model);
		Resource relationObject = model.listStatements(null, TREE_RELATION, (Resource) null).nextStatement().getObject()
				.asResource();
		verifyRelationStatements(model, relationObject);
		verifyMemberStatements(model);
	}

	private void verifyRelationStatements(Model model, Resource relationObject) {
		assertEquals(String.format(
				"[http://localhost:8080/view, https://w3id.org/tree#relation, %s]",
				relationObject),
				model.listStatements(createResource(FRAGMENT_ID), TREE_RELATION, (Resource) null).nextStatement()
						.toString());
		assertEquals(String.format("[%s, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, relation]", relationObject),
				model.listStatements(relationObject, RDF_SYNTAX_TYPE, (Resource) null).nextStatement().toString());
		assertEquals(String.format("[%s, https://w3id.org/tree#path, path]",
				relationObject),
				model.listStatements(relationObject, TREE_PATH, (Resource) null).nextStatement().toString());
		assertEquals(String.format("[%s, https://w3id.org/tree#node, http://localhost:8080/node]",
				relationObject),
				model.listStatements(relationObject, TREE_NODE, (Resource) null).nextStatement().toString());
		assertEquals(
				String.format("[%s, https://w3id.org/tree#value, \"value\"^^http://www.w3.org/2001/XMLSchema#dateTime]",
						relationObject),
				model.listStatements(relationObject, TREE_VALUE, (Resource) null).nextStatement().toString());
	}

	private void verifyMemberStatements(Model model) {
		assertEquals(
				"[http://localhost:8080/mobility-hindrances, https://w3id.org/tree#member, https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10228622/165]",
				model.listStatements(null, TREE_MEMBER, (Resource) null).nextStatement().toString());
	}

	private int getNumberOfStatements(Model model) {
		AtomicInteger statementCounter = new AtomicInteger();
		model.listStatements().forEach((statement) -> statementCounter.getAndIncrement());
		return statementCounter.get();
	}

	private void verifyTreeNodeStatement(Model model) {
		assertEquals(
				"[http://localhost:8080/view, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, https://w3id.org/tree#Node]",
				model.listStatements(null, RDF_SYNTAX_TYPE,
						createResource(TREE_NODE_RESOURCE)).nextStatement()
						.toString());
	}

}
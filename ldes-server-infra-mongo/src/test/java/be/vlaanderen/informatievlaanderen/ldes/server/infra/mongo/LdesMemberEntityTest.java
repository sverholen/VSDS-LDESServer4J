package be.vlaanderen.informatievlaanderen.ldes.server.infra.mongo;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesmember.entities.LdesMember;
import be.vlaanderen.informatievlaanderen.ldes.server.infra.mongo.entities.LdesMemberEntity;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LdesMemberEntityTest {

	private LdesMember ldesMember;
	private LdesMemberEntity ldesMemberEntity;

	@BeforeEach
	public void init() throws IOException, URISyntaxException {
		ClassLoader classLoader = getClass().getClassLoader();
		ldesMember = readLdesMemberFromFile(classLoader, "example-ldes-member.nq");
		ldesMemberEntity = readLdesMemberEntityFromFile(classLoader, "example-ldes-member-entity.json");
	}

	@Test
	@DisplayName("Convert LdesMember to LdesMemberEntity")
	void toEntity() {
		LdesMemberEntity actualLdesMemberEntity = LdesMemberEntity.fromLdesMember(ldesMember);

		Model actualLdesMemberEntityModel = RDFParserBuilder.create().fromString(actualLdesMemberEntity.getLdesMember())
				.lang(Lang.NQUADS).toModel();

		Model ldesMemberEntityModel = RDFParserBuilder.create().fromString(ldesMemberEntity.getLdesMember())
				.lang(Lang.NQUADS).toModel();

		assertTrue(ldesMemberEntityModel.isIsomorphicWith(actualLdesMemberEntityModel));
	}

	@Test
	@DisplayName("Convert LdesMemberEntity to LdesMember")
	void fromEntity() {
		LdesMember actualLdesMember = ldesMemberEntity.toLdesMember();
		assertTrue(ldesMember.getModel().isIsomorphicWith(actualLdesMember.getModel()));
	}

	private LdesMemberEntity readLdesMemberEntityFromFile(ClassLoader classLoader, String fileName)
			throws IOException, URISyntaxException {
		File file = new File(Objects.requireNonNull(classLoader.getResource(fileName)).toURI());

		Model outputModel = RDFParserBuilder.create()
				.fromString(Files.lines(Paths.get(file.toURI())).collect(Collectors.joining())).lang(Lang.JSONLD11)
				.toModel();

		return LdesMemberEntity.fromLdesMember(new LdesMember(
				"https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10810464/1", outputModel));
	}

	private LdesMember readLdesMemberFromFile(ClassLoader classLoader, String fileName)
			throws URISyntaxException, IOException {
		File file = new File(Objects.requireNonNull(classLoader.getResource(fileName)).toURI());

		Model outputModel = RDFParserBuilder.create()
				.fromString(Files.lines(Paths.get(file.toURI())).collect(Collectors.joining())).lang(Lang.NQUADS)
				.toModel();

		return new LdesMember("https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10810464/1",
				outputModel);
	}
}
package be.vlaanderen.informatievlaanderen.ldes.server.infra.mongo;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.converter.RdfModelConverter;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesmember.entities.LdesMember;
import be.vlaanderen.informatievlaanderen.ldes.server.infra.mongo.entities.LdesMemberEntity;
import be.vlaanderen.informatievlaanderen.ldes.server.infra.mongo.repositories.LdesMemberEntityRepository;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static be.vlaanderen.informatievlaanderen.ldes.server.domain.constants.RdfConstants.TREE_MEMBER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LdesMemberMongoRepositoryTest {
	private final LdesMemberEntityRepository ldesMemberEntityRepository = mock(LdesMemberEntityRepository.class);
	private final LdesMemberMongoRepository ldesMemberMongoRepository = new LdesMemberMongoRepository(
			ldesMemberEntityRepository);

	@DisplayName("Correct saving of an LdesMember in MongoDB")
	@Test
	void when_LdesMemberIsSavedInRepository_CreatedResourceIsReturned() {
		String member = String.format("""
				<http://one.example/subject1> <%s> <http://one.example/object1>.""", TREE_MEMBER);

		LdesMember ldesMember = new LdesMember("some_id", RdfModelConverter.fromString(member, Lang.NQUADS));
		LdesMemberEntity ldesMemberEntity = LdesMemberEntity.fromLdesMember(ldesMember);
		when(ldesMemberEntityRepository.save(any())).thenReturn(ldesMemberEntity);

		LdesMember actualLdesMember = ldesMemberMongoRepository.saveLdesMember(ldesMember);

		assertTrue(ldesMember.getModel().isIsomorphicWith(actualLdesMember.getModel()));
		verify(ldesMemberEntityRepository, times(1)).save(any());
	}

	@DisplayName("Correct retrieval of LdesMembers by Id from MongoDB")
	@Test
	void when_LdesMemberByIdIsRequested_LdesMemberIsReturnedWhenExisting() {
		String memberId = "https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10228622/165";
		Model ldesMemberModel = RDFParserBuilder.create().fromString(
				"""
						<http://localhost:8080/mobility-hindrances> <https://w3id.org/tree#member> <https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10228622/165> .""")
				.lang(Lang.NQUADS).toModel();
		LdesMember expectedLdesMember = new LdesMember(memberId, ldesMemberModel);
		LdesMemberEntity ldesMemberEntity = LdesMemberEntity.fromLdesMember(expectedLdesMember);

		when(ldesMemberEntityRepository.findById(memberId)).thenReturn(Optional.of(ldesMemberEntity));

		Optional<LdesMember> actualLdesMember = ldesMemberMongoRepository.getLdesMemberById(memberId);

		assertTrue(actualLdesMember.isPresent());
		assertEquals(expectedLdesMember.getLdesMemberId(),
				actualLdesMember.get().getLdesMemberId());
		verify(ldesMemberEntityRepository, times(1)).findById(memberId);
	}

	@DisplayName("Throwing of LdesMemberNotFoundException")
	@Test
	void when_LdesMemberByIdAndLdesMemberDoesNotExist_EmptyOptionalIsReturned() {
		String memberId = "https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10228622/165";
		when(ldesMemberEntityRepository.findById(memberId)).thenReturn(Optional.empty());

		Optional<LdesMember> ldesMemberById = ldesMemberMongoRepository.getLdesMemberById(memberId);

		assertFalse(ldesMemberById.isPresent());
		verify(ldesMemberEntityRepository, times(1)).findById(memberId);
	}

	@DisplayName("Correct retrieval of LdesMembers by Ids from MongoDB")
	@Test
	void when_LdesMemberByIdsIsRequested_LdesMembersAreReturned() {
		String memberId = "https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10228622/165";
		Model ldesMemberModel = RDFParserBuilder.create().fromString(
				"""
						<http://localhost:8080/mobility-hindrances> <https://w3id.org/tree#member> <https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10228622/165> .""")
				.lang(Lang.NQUADS).toModel();
		LdesMember expectedLdesMember = new LdesMember(memberId, ldesMemberModel);
		LdesMemberEntity ldesMemberEntity = LdesMemberEntity.fromLdesMember(expectedLdesMember);

		when(ldesMemberEntityRepository.findById(memberId)).thenReturn(Optional.of(ldesMemberEntity));

		Optional<LdesMember> actualLdesMember = ldesMemberMongoRepository.getLdesMemberById(memberId);

		assertTrue(actualLdesMember.isPresent());
		assertEquals(expectedLdesMember.getLdesMemberId(),
				actualLdesMember.get().getLdesMemberId());
		verify(ldesMemberEntityRepository, times(1)).findById(memberId);
	}
}
package be.vlaanderen.informatievlaanderen.ldes.server.fragmenters.mediator;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesfragment.services.FragmentationExecutor;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesfragment.services.FragmentationMediator;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesmember.entities.LdesMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectFragmentationMediator implements FragmentationMediator {
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectFragmentationMediator.class);
	private final FragmentationExecutor fragmentationExecutor;

	public DirectFragmentationMediator(FragmentationExecutor fragmentationExecutor) {
		LOGGER.info("Server has been configured to NOT use a queue for fragmentation");
		this.fragmentationExecutor = fragmentationExecutor;
	}

	@Override
	public void addMemberToFragment(LdesMember ldesMember) {
		fragmentationExecutor.executeFragmentation(ldesMember);
	}
}

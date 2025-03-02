package be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesfragment.valueobjects;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesfragmentrequest.valueobjects.FragmentPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class FragmentInfo {

	private final String viewName;
	private final List<FragmentPair> fragmentPairs;
	private Boolean immutable;

	public FragmentInfo(final String viewName, final List<FragmentPair> fragmentPairs) {
		this.viewName = viewName;
		this.fragmentPairs = fragmentPairs;
		this.immutable = false;
	}

	public FragmentInfo(String viewName, List<FragmentPair> fragmentPairs, Boolean immutable) {
		this.viewName = viewName;
		this.fragmentPairs = fragmentPairs;
		this.immutable = immutable;
	}

	public Optional<String> getValueOfKey(String key) {
		return fragmentPairs
				.stream()
				.filter(fragmentPair -> fragmentPair.fragmentKey().equals(key))
				.map(FragmentPair::fragmentValue)
				.findFirst();
	}

	public List<FragmentPair> getFragmentPairs() {
		return fragmentPairs;
	}

	public String getViewName() {
		return viewName;
	}

	public Boolean getImmutable() {
		return immutable;
	}

	public void setImmutable(Boolean immutable) {
		this.immutable = immutable;
	}

	public FragmentInfo createChild(FragmentPair fragmentPair) {
		ArrayList<FragmentPair> childFragmentPairs = new ArrayList<>(this.fragmentPairs.stream().toList());
		childFragmentPairs.add(fragmentPair);
		return new FragmentInfo(viewName, childFragmentPairs);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		FragmentInfo that = (FragmentInfo) o;
		return Objects.equals(viewName, that.viewName) && Objects.equals(fragmentPairs, that.fragmentPairs)
				&& Objects.equals(immutable, that.immutable);
	}

	@Override
	public int hashCode() {
		return Objects.hash(viewName, fragmentPairs, immutable);
	}

	public String generateFragmentId() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder
				.append("/").append(viewName);

		if (!fragmentPairs.isEmpty()) {
			stringBuilder.append("?");
			stringBuilder
					.append(fragmentPairs.stream().map(fragmentPair -> fragmentPair.fragmentKey() +
							"=" + fragmentPair.fragmentValue()).collect(Collectors.joining("&")));
		}

		return stringBuilder.toString();
	}
}

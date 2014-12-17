package org.reific.braid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Knots {

	private static final int DEFAULT_BYTE_BUFFER_SIZE = 128;
	private static final float DEFAULT_GROWTH_FACTOR = 1.5f;

	private Knots() {
	}

	public interface KnotBuilder {
		/**
		 * Specify a list of Strings that are expected to be frequently stored in the resulting Knot. The precise behavior of how the
		 * Knot will make use of this information is not specified, however, in general, it is likely that a request for {@link Braid}s of
		 * these values will return a constant {@link Braid} wrapper of the {@link String}, which should reduce memory and cpu usage, 
		 * as no (de-)compression will be required.
		 */
		KnotBuilder common(String commonString, String... moreCommonStrings);
		KnotBuilder lz78(int initialBufferSize, float bufferGrowthFactor);
		KnotBuilder rememberLast(int num);
		KnotBuilder direct();
		KnotBuilder lz78(int initialBufferSize);
		Knot build();
	}

	private static class KnotBuilderImpl implements KnotBuilder {

		private boolean built = false;
		private final HashSet<String> commonStrings = new HashSet<String>();
		private boolean direct = false;
		private int rememberLast = 0;
		private int lz78BufferSize = DEFAULT_BYTE_BUFFER_SIZE;
		private float lz78GrowthFactor = DEFAULT_GROWTH_FACTOR;

		@Override
		public KnotBuilder common(String commonString, String... remainingCommonStrings) {
			checkAlreadyBuilt();
			commonStrings.add(commonString);
			Collections.addAll(commonStrings, remainingCommonStrings);
			return this;
		}

		@Override
		public KnotBuilder rememberLast(int num) {
			checkAlreadyBuilt();
			rememberLast = num;
			return this;

		}

		@Override
		public KnotBuilder lz78(int initialBufferSize) {
			checkAlreadyBuilt();
			this.lz78BufferSize = initialBufferSize;
			return this;
		}

		@Override
		public KnotBuilder lz78(int initialBufferSize, float bufferGrowthFactor) {
			checkAlreadyBuilt();
			this.lz78BufferSize = initialBufferSize;
			this.lz78GrowthFactor = bufferGrowthFactor;
			return this;
		}

		public Knot build() {
			checkAlreadyBuilt();
			built = true;
			Buffer buffer = new AutoGrowingByteBuffer(lz78BufferSize, lz78GrowthFactor, direct);

			List<MutableInterner> mutableInterners = new ArrayList<MutableInterner>();
			List<Interner> internerList = new ArrayList<Interner>(1);
			if (!commonStrings.isEmpty()){
				internerList.add(new CommonStringInterner(commonStrings));
				mutableInterners.add(new MutableCommonStringInterner(commonStrings));
			}
			Interners interners = new Interners(internerList);

			List<Rememberer> remembererList = new ArrayList<Rememberer>();
			if (rememberLast == 1) {
				remembererList.add(new LastOneStringRemember());
			}
			Rememberers rememberers = new Rememberers(remembererList);

			return new KnotImpl(interners, new LZ78KnotStorage(buffer), rememberers, mutableInterners);
		}

		@Override
		public KnotBuilder direct() {
			checkAlreadyBuilt();
			this.direct = true;
			return this;
		}

		private void checkAlreadyBuilt() {
			if (built) {
				throw new IllegalStateException("Already built. A KnotBuilder can be used to build a single Knot");
			}

		}
	}

	public static KnotBuilder builder() {
		return new KnotBuilderImpl();
	}

}

/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.reific.jmh;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.reific.braid.Braid;
import org.reific.braid.Knot;
import org.reific.braid.Knots;

public class BraidJmhBenchmarks {

	private static final int LOOP_ITERATIONS = 10000;

	static class Pojo {
		private final String field;

		public Pojo(String value) {
			this.field = value;
		}

		public String getField() {
			return field;
		}
	}

	static class BraidPojo {
		private final Braid field;

		public BraidPojo(Braid value) {
			this.field = value;
		}

		public String getField() {
			return field.get();
		}
	}

	//	@State(Scope.Benchmark)
	//	public static class StandardKnot {
	//		final Knot knot = Knots.builder().build();
	//	}
	//
	//	@State(Scope.Benchmark)
	//	public static class RememberLastKnot {
	//		final Knot knot = Knots.builder().rememberLast(1).build();
	//	}
	//
	//	@State(Scope.Benchmark)
	//	public static class CommonWordsKnot {
	//		final Knot knot = Knots.builder().common("Hello World").build();
	//	}
	//
	//	@State(Scope.Benchmark)
	//	public static class Common2WordsKnot {
	//		final Knot knot = Knots.builder().common("Hello World", "Other").build();
	//	}
	//
	//	@State(Scope.Benchmark)
	//	public static class DirectKnot {
	//		final Knot knot = Knots.builder().direct().build();
	//	}
	//
	//
	//	@Benchmark
	//	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	//	public void standardKnotAddBraid(StandardKnot knotToUse) {
	//		knotToUse.knot.braid(new String("Hello World"));
	//	}
	//
	//	@Benchmark
	//	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	//	public void rememberLastKnotAddBraid(RememberLastKnot knotToUse) {
	//		knotToUse.knot.braid(new String("Hello World"));
	//    }
	//
	//	@Benchmark
	//	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	//	public void commonWordsKnotAddBraid(CommonWordsKnot knotToUse) {
	//		knotToUse.knot.braid(new String("Hello World"));
	//	}
	//
	//	@Benchmark
	//	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	//	public void common2WordsKnotAddBraid(Common2WordsKnot knotToUse) {
	//		knotToUse.knot.braid(new String("Hello World"));
	//	}
	//
	//	@Benchmark
	//	@OutputTimeUnit(TimeUnit.MICROSECONDS)
	//	public void directKnotAddBraid(DirectKnot knotToUse) {
	//		knotToUse.knot.braid(new String("Hello World"));
	//	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.SECONDS)
	public List<Pojo> pojoStringSame() {
		List<Pojo> list = new ArrayList<Pojo>();
		for (int i = 0; i < LOOP_ITERATIONS; i++) {
			String value = new String("Hello World");
			list.add(new Pojo(value));
		}
		return list;
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.SECONDS)
	public List<BraidPojo> pojoBraidSame() {
		Knot knot = Knots.builder().build();
		List<BraidPojo> list = new ArrayList<BraidPojo>();
		for (int i = 0; i < LOOP_ITERATIONS; i++) {
			Braid braid = knot.braid(new String("Hello World"));
			list.add(new BraidPojo(braid));
		}
		return list;
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.SECONDS)
	public List<BraidPojo> pojoBraidSameDirect() {
		Knot knot = Knots.builder().direct().build();
		List<BraidPojo> list = new ArrayList<BraidPojo>();
		for (int i = 0; i < LOOP_ITERATIONS; i++) {
			Braid braid = knot.braid(new String("Hello World"));
			list.add(new BraidPojo(braid));
		}
		return list;
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.SECONDS)
	public List<BraidPojo> pojoBraidSameCommon() {
		Knot knot = Knots.builder().common("Hello World").build();
		List<BraidPojo> list = new ArrayList<BraidPojo>();
		for (int i = 0; i < LOOP_ITERATIONS; i++) {
			Braid braid = knot.braid(new String("Hello World"));
			list.add(new BraidPojo(braid));
		}
		return list;
	}
	@Benchmark
	@OutputTimeUnit(TimeUnit.SECONDS)
	public List<BraidPojo> pojoBraidSameCommonTwo() {
		Knot knot = Knots.builder().common("Hello World", "Other").build();
		List<BraidPojo> list = new ArrayList<BraidPojo>();
		for (int i = 0; i < LOOP_ITERATIONS; i++) {
			Braid braid = knot.braid(new String("Hello World"));
			list.add(new BraidPojo(braid));
		}
		return list;
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.SECONDS)
	public List<BraidPojo> pojoBraidSameRememberLastOne() {
		Knot knot = Knots.builder().rememberLast(1).build();
		List<BraidPojo> list = new ArrayList<BraidPojo>();
		for (int i = 0; i < LOOP_ITERATIONS; i++) {
			Braid braid = knot.braid(new String("Hello World"));
			list.add(new BraidPojo(braid));
		}
		return list;
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.SECONDS)
	public List<BraidPojo> pojoBraidSameRememberLastTwo() {
		Knot knot = Knots.builder().rememberLast(2).build();
		List<BraidPojo> list = new ArrayList<BraidPojo>();
		for (int i = 0; i < LOOP_ITERATIONS; i++) {
			Braid braid = knot.braid(new String("Hello World"));
			list.add(new BraidPojo(braid));
		}
		return list;
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.SECONDS)
	public List<Pojo> pojoStringRandom() {
		List<Pojo> list = new ArrayList<Pojo>();
		for (int i = 0; i < LOOP_ITERATIONS; i++) {
			String value = UUID.randomUUID().toString();
			list.add(new Pojo(value));
		}
		return list;
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.SECONDS)
	public List<BraidPojo> pojoBraidRandom() {
		Knot knot = Knots.builder().build();
		List<BraidPojo> list = new ArrayList<BraidPojo>();
		for (int i = 0; i < LOOP_ITERATIONS; i++) {
			Braid braid = knot.braid(UUID.randomUUID().toString());
			list.add(new BraidPojo(braid));
		}
		return list;
	}

	@State(Scope.Benchmark)
	public static class PermutedStringState {
		String string = UUID.randomUUID().toString();
		private long nextIndex = 0;

		/**
		 * return a new String with one of it's characters arbitrarily increased by one (mod 256, to keep the data in the UTF-8 range, to simulate english)
		 */
		String nextPermutation() {
			char[] charArray = string.toCharArray();
			int index = (int) (nextIndex++ % charArray.length);
			charArray[index] = (char) ((charArray[index] + 1) % 256);
			string = new String(charArray);
			return string;
		}
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.SECONDS)
	public List<Pojo> pojoStringPermuted(PermutedStringState state) {
		List<Pojo> list = new ArrayList<Pojo>();
		for (int i = 0; i < LOOP_ITERATIONS; i++) {
			String value = state.nextPermutation();
			list.add(new Pojo(value));
		}
		return list;
	}

	@Benchmark
	@OutputTimeUnit(TimeUnit.SECONDS)
	public List<BraidPojo> pojoBraidPermuted(PermutedStringState state) {
		Knot knot = Knots.builder().build();
		List<BraidPojo> list = new ArrayList<BraidPojo>();
		for (int i = 0; i < LOOP_ITERATIONS; i++) {
			Braid braid = knot.braid(state.nextPermutation());
			list.add(new BraidPojo(braid));
		}
		return list;
	}

	static class StringPermuter {

	}

}

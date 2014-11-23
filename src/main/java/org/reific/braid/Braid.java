package org.reific.braid;

/**
 * <p align="left">
 * <img src="logo.jpg"/>
 * </p>
 * Braid library provides transparent in-memory compression of java Strings,
 * using a flyweight class {@link Braid}.
 * <p>
 * 
 * A group of {@link Braid}s is generally associated with a single {@link Knot}.
 * The actual (normally compressed) data of the Braids is stored in the
 * associated Knot.
 * 
 * <p>
 * This javadoc content is also available on the <a href=
 * "http://reific.github.io/braid/javadoc/apidocs/org/reific/braid/Braid.html"
 * >Braid</a> web page. All documentation is kept in javadocs because it
 * guarantees consistency between what's on the web and what's in the source
 * code. Also, it makes possible to access documentation straight from the IDE
 * even if you work offline. This documentation pattern was inspired by the
 * excellent mocking library <a href="http://mockito.org">Mockito</a>
 *
 * <h1>Contents</h1>
 *
 * <b> <a href="#1">1. Introduction </a><br/>
 * </b>
 *
 * <p>
 *
 * <h3 id="1">1. <a class="meaningful_link"
 * href="#introduction">Introduction</a></h3>
 *
 * <pre class="code">
 * <code class="java">
 * Knot knot = Knots.example();
 * String string = new String("test");
 * Braid braid = Braids.newExampleBraid(knot, string);
 * assertEquals(string,braid.get());
 * 
 * </code>
 * </pre>
 *
 * <p>
 * 
 * <p>
 *
 *
 *
 */
public interface Braid {

	/**
	 * 
	 * @return the {@link String} data that is encoded in this {@link Braid}.
	 *         Normally, this will delegate to an associated {@link Knot}, to
	 *         extract the compressed data.
	 */
	String get();

}

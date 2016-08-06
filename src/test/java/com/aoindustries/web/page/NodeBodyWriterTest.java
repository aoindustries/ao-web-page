/*
 * ao-web-page - Java API for modeling web page content and relationships.
 * Copyright (C) 2015, 2016  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-web-page.
 *
 * ao-web-page is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-web-page is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-web-page.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.web.page;

import java.io.IOException;
import java.io.StringWriter;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class NodeBodyWriterTest {

	private static final String TEST_BODY_PREFIX = "<TestNode>Test body <";
	private static final String TEST_ELEMENT_BODY = "<TestElement />";
	private static final String TEST_BODY_SUFFIX =
		"<" + NodeBodyWriter.MARKER_PREFIX + "ffffffffffffffff" + NodeBodyWriter.MARKER_SUFFIX
		+ "</TestNode>"
		+ NodeBodyWriter.MARKER_PREFIX + "ffffffff";

	private static final String TEST_EXPECTED_RESULT = TEST_BODY_PREFIX + TEST_ELEMENT_BODY + TEST_BODY_SUFFIX;

	private static Node testNode;
	private static String testNodeBody;

	private static final ElementContext nullElementContext = (resource, out) -> {
		// Do nothing
	};

	@BeforeClass
	public static void setUpClass() throws IOException {
		testNode = new Node() {
			@Override
			public String getLabel() {
				return "Test Node";
			}
			@Override
			public String getListItemCssClass() {
				return "test_item";
			}
		};
		Long elementKey = testNode.addChildElement(
			new Element() {
				@Override
				public String getLabel() {
					return "Test Element";
				}
				@Override
				protected String getDefaultIdPrefix() {
					return "test";
				}
				@Override
				public String getListItemCssClass() {
					return "test_element";
				}
				@Override
				public String getLinkCssClass() {
					return "testLink";
				}
			},
			(out, context) -> out.write(TEST_ELEMENT_BODY)
		);
		StringBuilder SB = new StringBuilder();
		SB.append(TEST_BODY_PREFIX);
		NodeBodyWriter.writeElementMarker(elementKey, SB);
		SB.append(TEST_BODY_SUFFIX);
		testNodeBody = SB.toString();
	}

	@AfterClass
	public static void tearDownClass() {
		testNode = null;
	}

	@Test
	public void testWriteElementMarker() throws Exception {
		//System.out.println(testNodeBody);
		//System.out.flush();
		final char[] testNodeBodyChars = testNodeBody.toCharArray();
		final int testNodeBodyLen = testNodeBody.length();
		for(int writeLen = 1; writeLen <= testNodeBodyLen; writeLen++) {
			for(int off = 0; off < writeLen; off++) {
				StringWriter out = new StringWriter(TEST_EXPECTED_RESULT.length());
				try {
					try (NodeBodyWriter writer = new NodeBodyWriter(testNode, out, nullElementContext)) {
						writer.write(testNodeBodyChars, 0, off);
						for(int pos = off; pos < testNodeBodyLen; pos += writeLen) {
							int end = pos + writeLen;
							if(end > testNodeBodyLen) end = testNodeBodyLen;
							int len = end - pos;
							assertTrue(len >= 0);
							assertTrue((pos + len) <= testNodeBodyLen);
							writer.write(testNodeBodyChars, pos, len);
						}
					}
				} finally {
					out.close();
				}
				assertEquals(TEST_EXPECTED_RESULT, out.toString());
			}
		}
	}
}
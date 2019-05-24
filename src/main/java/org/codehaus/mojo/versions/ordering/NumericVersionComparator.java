package org.codehaus.mojo.versions.ordering;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import java.math.BigInteger;
import java.util.StringTokenizer;

/**
 * A comparator which will compare all segments of a dot separated version
 * string as numbers if possible, i.e. 1.3.34 &gt; 1.3.9 and 1.3.4.3.2.34 &gt;
 * 1.3.4.3.2.9 and 1.3.4.3.2.34 &gt; 1.3.4.3.2.34-SNAPSHOT
 *
 * @author Stephen Connolly
 * @since 1.0-alpha-3
 * @deprecated
 */
public class NumericVersionComparator extends AbstractVersionComparator {
	private static final BigInteger BIG_INTEGER_ZERO = new BigInteger("0");

	private static final BigInteger BIG_INTEGER_ONE = new BigInteger("1");

	/**
	 * {@inheritDoc}
	 */
	public int compare(ArtifactVersion o1, ArtifactVersion o2) {
		String v1 = o1.toString();
		String v2 = o2.toString();
		StringTokenizer tok1 = new StringTokenizer(v1, ".");
		StringTokenizer tok2 = new StringTokenizer(v2, ".");
		while (tok1.hasMoreTokens() && tok2.hasMoreTokens()) {
			String p1 = tok1.nextToken();
			String p2 = tok2.nextToken();
			String q1 = null;
			String q2 = null;
			if (p1.indexOf('-') >= 0) {
				int index = p1.indexOf('-');
				q1 = p1.substring(index);
				p1 = p1.substring(0, index);
			}
			if (p2.indexOf('-') >= 0) {
				int index = p2.indexOf('-');
				q2 = p2.substring(index);
				p2 = p2.substring(0, index);
			}
			try {
				BigInteger n1 = new BigInteger(p1);
				BigInteger n2 = new BigInteger(p2);
				int result = n1.compareTo(n2);
				if (result != 0) {
					return result;
				}
			} catch (NumberFormatException e) {
				int result = p1.compareTo(p2);
				if (result != 0) {
					return result;
				}
			}
			if (q1 != null && q2 != null) {
				final int result = q1.compareTo(q2);
				if (result != 0) {
					return result;
				}
			}
			if (q1 != null) {
				return -1;
			}
			if (q2 != null) {
				return +1;
			}
		}
		if (tok1.hasMoreTokens()) {
			BigInteger n2 = BIG_INTEGER_ZERO;
			while (tok1.hasMoreTokens()) {
				try {
					BigInteger n1 = new BigInteger(tok1.nextToken());
					int result = n1.compareTo(n2);
					if (result != 0) {
						return result;
					}
				} catch (NumberFormatException e) {
					// any token is better than zero
					return +1;
				}
			}
			return -1;
		}
		if (tok2.hasMoreTokens()) {
			BigInteger n1 = BIG_INTEGER_ZERO;
			while (tok2.hasMoreTokens()) {
				try {
					BigInteger n2 = new BigInteger(tok2.nextToken());
					int result = n1.compareTo(n2);
					if (result != 0) {
						return result;
					}
				} catch (NumberFormatException e) {
					// any token is better than zero
					return -1;
				}
			}
			return +1;
		}
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	protected int innerGetSegmentCount(ArtifactVersion v) {
		final String version = v.toString();
		StringTokenizer tok = new StringTokenizer(version, ".");
		return tok.countTokens();
	}

	/**
	 * {@inheritDoc}
	 */
	protected ArtifactVersion innerIncrementSegment(ArtifactVersion v, int segment) {
		final int segmentCount = innerGetSegmentCount(v);
		if (segment < 0 || segment > segmentCount) {
			throw new InvalidSegmentException(segment, segmentCount, v.toString());
		}
		final String version = v.toString();
		StringBuilder buf = new StringBuilder();
		StringTokenizer tok = new StringTokenizer(version, ".");
		boolean first = true;
		int seg = segment;
		while (seg >= 0 && tok.hasMoreTokens()) {
			first = bufAppend3(buf, first);
			String[] pq = {tok.nextToken(), null};
			if (pq[0].indexOf('-') >= 0) {
				int index = pq[0].indexOf('-');
				pq[1] = pq[0].substring(index + 1);
				pq[0] = pq[0].substring(0, index);
			}
			if (seg == 0) {
				tryCatch(pq);
			}
			seg = bufAppend1(buf, seg, pq[0], pq[1]);
		}
		first = bufAppend2(buf, tok, first);
		return new DefaultArtifactVersion(buf.toString());
	}

	private void tryCatch(String[] pq) {
		try {
			BigInteger n = new BigInteger(pq[0]);
			pq[0] = n.add(BIG_INTEGER_ONE).toString();
			pq[1] = null;
		} catch (NumberFormatException e) {
			// ok, let's try some common tricks
			if ("alpha".equalsIgnoreCase(pq[0])) {
				case1(pq);
			} else if ("beta".equalsIgnoreCase(pq[0])) {
				case2(pq);
			} else if ("milestone".equalsIgnoreCase(pq[0])) {
				case3(pq);
			} else if ("cr".equalsIgnoreCase(pq[0]) || "rc".equalsIgnoreCase(pq[0])) {
				case4(pq);
			} else if ("ga".equalsIgnoreCase(pq[0]) || "final".equalsIgnoreCase(pq[0])) {
				case5(pq);
			} else {
				pq[0] = VersionComparators.alphaNumIncrement(pq[0]);
			}
		}
	}

	private void case5(String[] pq) {
		if (pq[1] == null) {
			pq[0] = "sp";
			pq[1] = "1";
		} else {
			try {
				BigInteger n = new BigInteger(pq[1]);
				pq[1] = n.add(BIG_INTEGER_ONE).toString();
			} catch (NumberFormatException e1) {
				pq[0] = "sp";
				pq[1] = "1";
			}
		}
	}

	private void case4(String[] pq) {
		if (pq[1] == null) {
			pq[0] = "ga";
		} else {
			try {
				BigInteger n = new BigInteger(pq[1]);
				pq[1] = n.add(BIG_INTEGER_ONE).toString();
			} catch (NumberFormatException e1) {
				pq[0] = "ga";
				pq[1] = null;
			}
		}
	}

	private void case3(String[] pq) {
		if (pq[1] == null) {
			pq[0] = "rc";
		} else {
			try {
				BigInteger n = new BigInteger(pq[1]);
				pq[1] = n.add(BIG_INTEGER_ONE).toString();
			} catch (NumberFormatException e1) {
				pq[0] = "rc";
				pq[1] = null;
			}
		}
	}

	private void case2(String[] pq) {
		if (pq[1] == null) {
			pq[0] = "milestone";
		} else {
			try {
				BigInteger n = new BigInteger(pq[1]);
				pq[1] = n.add(BIG_INTEGER_ONE).toString();
			} catch (NumberFormatException e1) {
				pq[0] = "milestone";
				pq[1] = null;
			}
		}
	}

	private void case1(String[] pq) {
		if (pq[1] == null) {
			pq[0] = "beta";
		} else {
			try {
				BigInteger n = new BigInteger(pq[1]);
				pq[1] = n.add(BIG_INTEGER_ONE).toString();
			} catch (NumberFormatException e1) {
				pq[0] = "beta";
				pq[1] = null;
			}
		}
	}

	private boolean bufAppend3(StringBuilder buf, boolean first) {
		if (first) {
			first = false;
		} else {
			buf.append('.');
		}
		return first;
	}

	private int bufAppend1(StringBuilder buf, int seg, String p, String q) {
		buf.append(p);
		if (q != null) {
			buf.append('-');
			buf.append(q);
		}
		seg--;
		return seg;
	}

	private boolean bufAppend2(StringBuilder buf, StringTokenizer tok, boolean first) {
		while (tok.hasMoreTokens()) {
			first = bufAppend3(buf, first);
			tok.nextToken();
			buf.append("0");
		}
		return first;
	}
}

package com.fluxcapacitor.core.test;

import java.util.Arrays;
import java.util.List;

public class BaseAppTest {
	public enum Option {
	}

	private static List<Option> optionsList;

	public BaseAppTest() {
	}

	public static void baseSetUp(Option... theOptions) throws Exception {
		if (theOptions == null) {
			return;
		}

		optionsList = Arrays.asList(theOptions);
	}

	public static void baseTearDown() throws Exception {
		if (optionsList == null) {
			return;
		}
	}
}
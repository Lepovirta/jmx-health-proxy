package org.lepovirta.jmxhealthproxy;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class ExpectationEval implements Predicate<String> {

    private enum Matcher implements BiPredicate<String, String> {
        EQ("=", (e, a) -> a.equals(e)),
        REGEX("~", (e, a) -> a.matches(e)),
        LTE("<=", (e, a) -> Double.parseDouble(a) <= Double.parseDouble(e)),
        GTE(">=", (e, a) -> Double.parseDouble(a) >= Double.parseDouble(e)),
        LT("<", (e, a) -> Double.parseDouble(a) < Double.parseDouble(e)),
        GT(">", (e, a) -> Double.parseDouble(a) > Double.parseDouble(e));

        private final String symbol;
        private final BiPredicate<String, String> predicate;

        Matcher(String symbol, BiPredicate<String, String> predicate) {
            this.symbol = symbol;
            this.predicate = predicate;
        }

        @Override
        public boolean test(String expected, String actual) {
            return predicate.test(expected, actual);
        }

        String parseExpectation(String s) {
            if (s.startsWith(symbol)) {
                return s.substring(symbol.length());
            }
            return s;
        }

        static Matcher fromString(String s) {
            for (Matcher matcher : Matcher.values()) {
                if (s.startsWith(matcher.symbol)) {
                    return matcher;
                }
            }
            return EQ;
        }
    }

    private final Matcher matcher;
    private final String expectation;

    public ExpectationEval(String expectation) {
        this.matcher = Matcher.fromString(expectation);
        this.expectation = matcher.parseExpectation(expectation);
    }

    @Override
    public boolean test(String s) {
        return matcher.test(expectation, s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpectationEval that = (ExpectationEval) o;
        return matcher == that.matcher &&
                Objects.equals(expectation, that.expectation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matcher, expectation);
    }
}

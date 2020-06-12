package org.s1ck.gdl;

import org.s1ck.gdl.model.comparables.ComparableExpression;
import org.s1ck.gdl.model.comparables.time.*;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static org.s1ck.gdl.utils.Comparator.*;

/**
 * Responsible for processing the temporal aspects of a query within {@link GDLLoader}
 */
public class GDLLoaderTemporal {

    /**
     * Holds predicates that might be created during processing of other predicates
     */
    private final Deque<Predicate> predicateStack;

    /**
     * GDLLoader that processes the whole query
     */
    private final GDLLoader loader;

    /**
     * Creates a new instance
     *
     * @param loader loader that processes the whole query
     */
    public GDLLoaderTemporal(GDLLoader loader) {
        this.predicateStack = new ArrayDeque<>();
        this.loader = loader;
    }

    /**
     * Builds a Comparison filter operator from comparison context
     *
     * @param ctx the comparison context that will be parsed
     * @return parsed operator
     */
    Comparison buildTemporalComparison(GDLParser.TemporalComparisonContext ctx) {
        ComparableExpression lhs = buildTimePoint(ctx.timePoint(0));
        ComparableExpression rhs = buildTimePoint(ctx.timePoint(1));
        Comparator comp = Comparator.fromString(ctx.ComparisonOP().getText());

        return new Comparison(lhs, comp, rhs);
    }

    /**
     * Builds a {@code TimePoint} (can be {@code TimeLiteral}, {@code TimeSelector}, {@code MIN},...) given its context
     *
     * @param ctx time point context
     * @return the {@code TimePoint} described by the context
     */
    private TimePoint buildTimePoint(GDLParser.TimePointContext ctx) {
        if (ctx.timeLiteral() != null) {
            return buildTimeLiteral(ctx.timeLiteral());
        } else if (ctx.timeSelector() != null) {
            return buildTimeSelector(ctx.timeSelector());
        } else if (ctx.complexTimePoint() != null) {
            return buildComplexTimePoint(ctx.complexTimePoint());
        }
        return null;
    }

    /**
     * Builds a TimeLiteral given a context.
     *
     * @param ctx context containing the literal
     * @return TimeLiteral
     */
    private TimeLiteral buildTimeLiteral(GDLParser.TimeLiteralContext ctx) {
        return new TimeLiteral(ctx.getText().trim());
    }

    /**
     * Builds a TimeSelector (variable.field, where field in {TX_FROM, TX_TO, VAL_FROM, VAL_TO})
     *
     * @param ctx context containing the selector
     * @return TimeSelector
     */
    private TimeSelector buildTimeSelector(GDLParser.TimeSelectorContext ctx) {
        // checks whether ID is even there (is a vertex or edge) and returns its variable
        String var = ctx.Identifier() != null ?
                loader.resolveIdentifier(ctx.Identifier().getText()) : TimeSelector.GLOBAL_SELECTOR;
        String field = ctx.TimeProp().getText();
        return new TimeSelector(var, field);
    }

    /**
     * Builds a "complex" time point, i.e. a time point described by a {@code MAX(...)} or
     * {@code MIN(...)} expression.
     *
     * @param ctx context containing the time point
     * @return complex time point
     */
    private TimePoint buildComplexTimePoint(GDLParser.ComplexTimePointContext ctx) {

        List<GDLParser.ComplexTimePointArgumentContext> argumentContexts =
                ctx.complexTimePointArgument();
        TimePoint[] args = new TimePoint[argumentContexts.size()];

        for (int i = 0; i < argumentContexts.size(); i++) {
            GDLParser.ComplexTimePointArgumentContext argumentContext = argumentContexts.get(i);
            if (argumentContext.timeLiteral() != null) {
                args[i] = buildTimeLiteral(argumentContext.timeLiteral());
            } else if (argumentContext.timeSelector() != null) {
                args[i] = buildTimeSelector(argumentContext.timeSelector());
            }
        }
        // available complex time points: min and max
        if (ctx.getText().startsWith("MAX(")) {
            return new MaxTimePoint(args);
        } else if (ctx.getText().startsWith("MIN(")) {
            return new MinTimePoint(args);
        }
        return null;
    }

    /**
     * Converts an interval function into a (complex) predicate
     * For example, i.between(x,y) would be translated to a predicate ((i.from<= y) AND (i.to>x))
     *
     * @param ctx interval function context
     * @return complex predicate that encodes the interval function. Atoms are time stamp comparisons
     */
    Predicate buildIntervalFunction(GDLParser.IntvFContext ctx) {
        int predicateSizeBefore = predicateStack.size();
        TimePoint[] intv = buildInterval(ctx.interval());
        TimePoint from = intv[0];
        TimePoint to = intv[1];
        Predicate predicate = createIntervalPredicates(from, to, ctx.intervalFunc());
        // additional constraints?
        int countConstraints = predicateStack.size() - predicateSizeBefore;
        for (int i = 0; i < countConstraints; i++) {
            predicate = new And(predicate, predicateStack.removeFirst());
        }
        return predicate;
    }

    /**
     * Creates a new predicate about an interval. There are different types of interval predicates/
     * functions: precedes, fromTo,...
     *
     * @param from         represents the start time (from value) of the interval
     * @param to           represents the end time (to value) of the interval
     * @param intervalFunc contains the context information needed to create the correct predicate
     * @return new predicate (according to {@code intervalFunc}) about the interval represented by
     * {@code from} and {@code to}.
     */
    private Predicate createIntervalPredicates(TimePoint from, TimePoint to, GDLParser.IntervalFuncContext intervalFunc) {
        if (intervalFunc.overlapsIntervallOperator() != null) {
            return createOverlapsPredicates(from, to, intervalFunc.overlapsIntervallOperator());
        } else if (intervalFunc.fromToOperator() != null) {
            return createFromToPredicates(from, to, intervalFunc.fromToOperator());
        } else if (intervalFunc.betweenOperator() != null) {
            return createBetweenPredicates(from, to, intervalFunc.betweenOperator());
        } else if (intervalFunc.precedesOperator() != null) {
            return createPrecedesPredicates(to, intervalFunc.precedesOperator());
        } else if (intervalFunc.succeedsOperator() != null) {
            return createSucceedsPredicates(from, intervalFunc.succeedsOperator());
        } else if (intervalFunc.containsOperator() != null) {
            return createContainsPredicates(from, to, intervalFunc.containsOperator());
        } else if (intervalFunc.immediatelyPrecedesOperator() != null) {
            return createImmediatelyPrecedesPredicates(to, intervalFunc.immediatelyPrecedesOperator());
        } else if (intervalFunc.immediatelySucceedsOperator() != null) {
            return createImmediatelySucceedsPredicates(from, intervalFunc.immediatelySucceedsOperator());
        } else if (intervalFunc.equalsOperator() != null) {
            return createEqualsPredicates(from, to, intervalFunc.equalsOperator());
        } else if (intervalFunc.longerThanOperator() != null) {
            return createLongerThanPredicates(from, to, intervalFunc.longerThanOperator());
        } else if (intervalFunc.shorterThanOperator() != null) {
            return createShorterThanPredicates(from, to, intervalFunc.shorterThanOperator());
        } else if (intervalFunc.lengthAtLeastOperator() != null) {
            return createLengthAtLeastPredicates(from, to, intervalFunc.lengthAtLeastOperator());
        } else if (intervalFunc.lengthAtMostOperator() != null) {
            return createLengthAtMostPredicates(from, to, intervalFunc.lengthAtMostOperator());
        }
        return null;
    }

    /**
     * Creates a predicate a.overlaps(b)=max(a.from,b.from)<min(a.to,b.to)
     *
     * @param from the from value of the calling interval
     * @param to   the to value of the calling interval
     * @param ctx  the context containing the called interval
     * @return overlaps predicate
     */
    private Predicate createOverlapsPredicates(TimePoint from, TimePoint to, GDLParser.OverlapsIntervallOperatorContext ctx) {
        TimePoint[] arg = buildInterval(ctx.interval());
        TimePoint arg_from = arg[0];
        TimePoint arg_to = arg[1];
        TimePoint mx = new MaxTimePoint(from, arg_from);
        TimePoint mn = new MinTimePoint(to, arg_to);
        return new Comparison(mx, Comparator.LT, mn);
    }


    /**
     * Creates a predicate a.fromTo(x,y)= a.from<y AND a.to>x
     *
     * @param from from value of the calling interval
     * @param to   to value of the calling interval
     * @param ctx  context of the call, containing x and y
     * @return fromTo predicate
     */
    private Predicate createFromToPredicates(TimePoint from, TimePoint to, GDLParser.FromToOperatorContext ctx) {
        TimePoint x = buildTimePoint(ctx.timePoint(0));
        TimePoint y = buildTimePoint(ctx.timePoint(1));
        return new And(
                new Comparison(from, Comparator.LT, y),
                new Comparison(to, Comparator.GT, x)
        );
    }

    /**
     * Creates a predicate a.between(x,y) = a.from<=y AND a.to>x
     *
     * @param from from value of the calling interval
     * @param to   to value of the calling interval
     * @param ctx  context of the call, containing x and y
     * @return between predicate
     */
    private Predicate createBetweenPredicates(TimePoint from, TimePoint to, GDLParser.BetweenOperatorContext ctx) {
        TimePoint x = buildTimePoint(ctx.timePoint(0));
        TimePoint y = buildTimePoint(ctx.timePoint(1));
        return new And(
                new Comparison(from, LTE, y),
                new Comparison(to, Comparator.GT, x)
        );
    }

    /**
     * Creates a predicate a.precedes(b) = a.to <= b.from.
     * Function is used for interval and timestamp function {@code precedes}, as they both
     * only compare two time stamps
     *
     * @param to  the time stamp of the caller to compare
     * @param ctx the context containing the value to be compared
     * @return precedes predicate
     */
    private Predicate createPrecedesPredicates(TimePoint to, GDLParser.PrecedesOperatorContext ctx) {
        TimePoint[] arg = buildInterval(ctx.interval());
        TimePoint arg_from = arg[0];
        return new Comparison(to, LTE, arg_from);
    }

    /**
     * Creates a predicate a.immediatelyPrecedes(b) = (a.to == b.from).
     *
     * @param to  the time stamp of the caller to compare
     * @param ctx the context containing the from value to be compared
     * @return immediatelyPrecedes predicate
     */
    private Predicate createImmediatelyPrecedesPredicates(TimePoint to, GDLParser.ImmediatelyPrecedesOperatorContext ctx) {
        TimePoint[] arg = buildInterval(ctx.interval());
        TimePoint arg_from = arg[0];
        return new Comparison(to, EQ, arg_from);
    }

    /**
     * Creates a predicate a.succeeds(b) = a >= b.
     * Function is used for interval and timestamp function {@code precedes}, as they both
     * only compare two time stamps
     *
     * @param point the time stamp of the caller to compare
     * @param ctx   the context containing the value to be compared
     * @return succeeds predicate
     */
    private Predicate createSucceedsPredicates(TimePoint point, GDLParser.SucceedsOperatorContext ctx) {
        TimePoint[] arg = buildInterval(ctx.interval());
        TimePoint arg_to = arg[1];
        return new Comparison(point, GTE, arg_to);
    }

    /**
     * Creates a predicate a.immediatelySucceeds(b) = (a.from == b.to).
     * Function is used for interval and timestamp function {@code precedes}, as they both
     * only compare two time stamps
     *
     * @param from the from value of the caller interval
     * @param ctx  the context containing the to value of the interval to be compared
     * @return immediatelySucceeds predicate
     */
    private Predicate createImmediatelySucceedsPredicates(TimePoint from,
                                                          GDLParser.ImmediatelySucceedsOperatorContext ctx) {
        TimePoint[] arg = buildInterval(ctx.interval());
        TimePoint arg_to = arg[1];
        return new Comparison(from, EQ, arg_to);
    }

    /**
     * Creates a predicate a.contains(b) = a.from<=b.from AND a.to>=b.to
     *
     * @param from from value of the calling interval
     * @param to   to value of the calling interval
     * @param ctx  context of the call, containing b
     * @return contains predicate
     */
    private Predicate createContainsPredicates(TimePoint from, TimePoint to, GDLParser.ContainsOperatorContext ctx) {
        if (ctx.interval() != null) {
            TimePoint[] arg = buildInterval(ctx.interval());
            TimePoint arg_from = arg[0];
            TimePoint arg_to = arg[1];
            return new And(
                    new Comparison(from, LTE, arg_from),
                    new Comparison(to, GTE, arg_to)
            );
        }
        // argument is only a timestamp
        else {
            TimePoint arg = buildTimePoint(ctx.timePoint());
            return new And(
                    new Comparison(from, LTE, arg), new Comparison(to, GTE, arg)
            );
        }
    }

    /**
     * Creates a predicate a.equals(b) = (a.from = b.from AND a.to = b.to).
     *
     * @param from from value of the calling interval
     * @param to   to value of the calling interval
     * @param ctx  context containing the callee interval
     * @return equals predicate
     */
    private Predicate createEqualsPredicates(TimePoint from, TimePoint to, GDLParser.EqualsOperatorContext ctx) {
        TimePoint[] arg = buildInterval(ctx.interval());
        TimePoint arg_from = arg[0];
        TimePoint arg_to = arg[1];
        return new And(
                new Comparison(from, EQ, arg_from),
                new Comparison(to, EQ, arg_to)
        );
    }

    /**
     * Creates a predicate a.longerThan(b) = (length(a) > length(b))
     * Implicitly adds constraints that ensure that durations are always positive
     *
     * @param from from value of the calling interval
     * @param to   to value of the calling interval
     * @param ctx  context containing the callee interval
     * @return longerThan predicate
     */
    private Predicate createLongerThanPredicates(TimePoint from, TimePoint to, GDLParser.LongerThanOperatorContext ctx) {
        Predicate durationPredicate;
        Duration rhs = new Duration(from, to);
        durationPredicate = new Comparison(from, LTE, to);
        if (ctx.timeConstant() != null) {
            TimeConstant constant = buildTimeConstant(ctx.timeConstant());
            durationPredicate = new And(durationPredicate,
                    new Comparison(rhs, GT, constant));
        } else if (ctx.interval() != null) {
            TimePoint[] interval = buildInterval(ctx.interval());
            Duration lhs = new Duration(interval[0], interval[1]);
            durationPredicate = new And(durationPredicate,
                    new Comparison(interval[0], LTE, interval[1]));
            durationPredicate = new And(durationPredicate,
                    new Comparison(rhs, GT, lhs));
        }
        return durationPredicate;
    }

    /**
     * Creates a predicate a.shorterThan(b) = (length(a) < length(b))
     * Implicitly adds constraints that ensure that durations are always positive
     *
     * @param from from value of the calling interval
     * @param to   to value of the calling interval
     * @param ctx  context containing the callee interval
     * @return shorterThan predicate
     */
    private Predicate createShorterThanPredicates(TimePoint from, TimePoint to, GDLParser.ShorterThanOperatorContext ctx) {
        Predicate durationPredicate;
        Duration rhs = new Duration(from, to);
        durationPredicate = new Comparison(from, LTE, to);
        if (ctx.timeConstant() != null) {
            TimeConstant constant = buildTimeConstant(ctx.timeConstant());
            durationPredicate = new And(durationPredicate,
                    new Comparison(rhs, LT, constant));
        } else if (ctx.interval() != null) {
            TimePoint[] interval = buildInterval(ctx.interval());
            Duration lhs = new Duration(interval[0], interval[1]);
            durationPredicate = new And(durationPredicate,
                    new Comparison(interval[0], LTE, interval[1]));
            durationPredicate = new And(durationPredicate,
                    new Comparison(rhs, LT, lhs));
        }
        return durationPredicate;
    }

    /**
     * Creates a predicate a.lengthAtLeast(b) = (length(a) >= length(b))
     * Implicitly adds constraints that ensure that durations are always positive
     *
     * @param from from value of the calling interval
     * @param to   to value of the calling interval
     * @param ctx  context containing the callee interval
     * @return lengthAtLeast predicate
     */
    private Predicate createLengthAtLeastPredicates(TimePoint from, TimePoint to,
                                                    GDLParser.LengthAtLeastOperatorContext ctx) {
        Predicate durationPredicate;
        Duration rhs = new Duration(from, to);
        durationPredicate = new Comparison(from, LTE, to);
        if (ctx.timeConstant() != null) {
            TimeConstant constant = buildTimeConstant(ctx.timeConstant());
            durationPredicate = new And(durationPredicate,
                    new Comparison(rhs, GTE, constant));
        } else if (ctx.interval() != null) {
            TimePoint[] interval = buildInterval(ctx.interval());
            Duration lhs = new Duration(interval[0], interval[1]);
            durationPredicate = new And(durationPredicate,
                    new Comparison(interval[0], LTE, interval[1]));
            durationPredicate = new And(durationPredicate,
                    new Comparison(rhs, GTE, lhs));
        }
        return durationPredicate;
    }

    /**
     * Creates a predicate a.lengthAtMost(b) = (length(a) <= length(b))
     * Implicitly adds constraints that ensure that durations are always positive
     *
     * @param from from value of the calling interval
     * @param to   to value of the calling interval
     * @param ctx  context containing the callee interval
     * @return lengthAtMost predicate
     */
    private Predicate createLengthAtMostPredicates(TimePoint from, TimePoint to,
                                                   GDLParser.LengthAtMostOperatorContext ctx) {
        Predicate durationPredicate;
        Duration rhs = new Duration(from, to);
        durationPredicate = new Comparison(from, LTE, to);
        if (ctx.timeConstant() != null) {
            TimeConstant constant = buildTimeConstant(ctx.timeConstant());
            durationPredicate = new And(durationPredicate,
                    new Comparison(rhs, LTE, constant));
        } else if (ctx.interval() != null) {
            TimePoint[] interval = buildInterval(ctx.interval());
            Duration lhs = new Duration(interval[0], interval[1]);
            durationPredicate = new And(durationPredicate,
                    new Comparison(interval[0], LTE, interval[1]));
            durationPredicate = new And(durationPredicate,
                    new Comparison(rhs, LTE, lhs));
        }
        return durationPredicate;
    }

    /**
     * Creates a TimeConstant given a suitable context. Constants can be a constant number
     * of days ({@code Days(n)}), hours ({@code Hours(n)}), minutes ({@code Minutes(n)}),
     * seconds ({@code Seconds(n)}) or milliseconds ({@code Millis(n)}).
     *
     * @param ctx the context containing the constant.
     * @return time constant
     */
    private TimeConstant buildTimeConstant(GDLParser.TimeConstantContext ctx) {
        int value = Integer.parseInt(ctx.IntegerLiteral().getText());
        if (ctx.getText().startsWith("Days(")) {
            return new TimeConstant(value, 0, 0, 0, 0);
        } else if (ctx.getText().startsWith("Hours(")) {
            return new TimeConstant(0, value, 0, 0, 0);
        } else if (ctx.getText().startsWith("Minutes(")) {
            return new TimeConstant(0, 0, value, 0, 0);
        } else if (ctx.getText().startsWith("Seconds(")) {
            return new TimeConstant(0, 0, 0, value, 0);
        } else if (ctx.getText().startsWith("Millis(")) {
            return new TimeConstant(0, 0, 0, 0, value);
        }
        return null;
    }

    /**
     * Creates an array {@code {from, to}} representing an interval.
     *
     * @param ctx context from which to derive {@code from} and {@code to}
     * @return {@code {from, to}} representing an interval
     */
    private TimePoint[] buildInterval(GDLParser.IntervalContext ctx) {
        if (ctx.intervalSelector() != null) {
            GDLParser.IntervalSelectorContext selector = ctx.intervalSelector();
            // throws exception, if variable invalid
            return buildIntervalFromSelector(selector);
        } else if (ctx.intervalFromStamps() != null) {
            GDLParser.IntervalFromStampsContext fs = ctx.intervalFromStamps();
            TimePoint[] intv = buildIntervalFromStamps(fs);
            // custom interval: make sure that from <= to
            predicateStack.add(new Comparison(intv[0], LTE, intv[1]));
            return intv;
        } else if (ctx.complexInterval() != null) {
            GDLParser.ComplexIntervalArgumentContext arg1 = ctx.complexInterval()
                    .complexIntervalArgument(0);
            GDLParser.ComplexIntervalArgumentContext arg2 = ctx.complexInterval()
                    .complexIntervalArgument(1);
            boolean join = ctx.getText().contains(".join(");
            return buildIntervalFromComplex(arg1, arg2, join);
        }
        return null;
    }

    /**
     * Creates an interval as an array {@code {from, to}} from a selector context.
     * I.e., a interval like {@code a.val} would result in {@code {a.val_from, a.val_to}}.
     * What is more, {@code val} results in {@code GLOBAL_SELECTOR.val_from, GLOBAL_SELECTOR.val_to}
     *
     * @param ctx context from which to derive the interval
     * @return {@code {from, to}} representing the interval
     */
    private TimePoint[] buildIntervalFromSelector(GDLParser.IntervalSelectorContext ctx) {
        String var = ctx.Identifier() != null ?
                loader.resolveIdentifier(ctx.Identifier().getText()) : TimeSelector.GLOBAL_SELECTOR;
        String intId = ctx.IntervalConst().getText();
        TimePoint from = new TimeSelector(var, intId + "_from");
        TimePoint to = new TimeSelector(var, intId + "_to");
        return new TimePoint[]{from, to};
    }

    /**
     * Creates an interval as an array {@code {from, to}} from a interval constant context.
     * I.e., a interval like {@code Interval(1970-01-01, 2020-01-01)} would result in
     * {@code {1970-01-01, 2020-01-01}}.
     *
     * @param ctx context from which to derive the interval
     * @return {@code {from, to}} representing the interval
     */
    private TimePoint[] buildIntervalFromStamps(GDLParser.IntervalFromStampsContext ctx) {
        TimePoint from = buildTimePoint(ctx.timePoint(0));
        TimePoint to = buildTimePoint(ctx.timePoint(1));
        return new TimePoint[]{from, to};
    }

    /**
     * Creates an interval as an array {@code {from, to}} from a complex interval context, i.e.
     * {@code merge} and {@code join} expressions.
     * An interval like {@code a.merge(b)} would result in {@code {max(a.from, b.from), min(a.to, b.to}}
     * while {@code a.join(b)} results in {@code {min(a.from, b.from), max(a.to, b.to)}}.
     * Furthermore, a constraint {max(a.from, b.from)<= min(a.to, b.to)} is added (intervals must overlap,
     * in at least one ms)
     *
     * @param arg1 context from which to derive the calling interval
     * @param arg2 context from which to derive the callee interval
     * @param join true iff join should be performed, false iff merge is desired
     * @return {@code {from, to}} representing the interval
     */
    private TimePoint[] buildIntervalFromComplex(GDLParser.ComplexIntervalArgumentContext arg1,
                                                 GDLParser.ComplexIntervalArgumentContext arg2,
                                                 boolean join) {
        TimePoint[] i1;
        TimePoint[] i2;
        if (arg1.intervalFromStamps() != null) {
            i1 = buildIntervalFromStamps(arg1.intervalFromStamps());
        } else {
            i1 = buildIntervalFromSelector(arg1.intervalSelector());
        }
        if (arg2.intervalFromStamps() != null) {
            i2 = buildIntervalFromStamps(arg2.intervalFromStamps());
        } else {
            i2 = buildIntervalFromSelector(arg2.intervalSelector());
        }
        // constraint: merge and join only when overlapping or meeting
        Comparison constraint = new Comparison(
                new MaxTimePoint(i1[0], i2[0]), LTE, new MinTimePoint(i1[1], i2[1])
        );
        predicateStack.addFirst(constraint);
        // now build complex interval from i1, i2
        if (join) {
            TimePoint start = new MinTimePoint(i1[0], i2[0]);
            TimePoint end = new MaxTimePoint(i1[1], i2[1]);
            return new TimePoint[]{start, end};
        }
        // merge
        else {
            TimePoint start = new MaxTimePoint(i1[0], i2[0]);
            TimePoint end = new MinTimePoint(i1[1], i2[1]);
            return new TimePoint[]{start, end};
        }
    }

    /**
     * Converts a time stamp function into a (potentially complex) {@code Predicate}
     * For example, i.before(x) would be translated to a {@code Predicate} i<x
     *
     * @param ctx time stamp function context
     * @return (potentially complex) {@code Predicate} that encodes the time stamp function. Atoms are time stamp comparisons
     */
    Predicate buildStampFunction(GDLParser.StmpFContext ctx) {
        TimePoint tp = buildTimePoint(ctx.timePoint());
        return createStampPredicates(tp, ctx.stampFunc());
    }

    /**
     * Returns time stamp {@code Predicate} given the caller (a time stamp) and its context
     *
     * @param tp        the caller
     * @param stampFunc context including the operator (e.g. before,...) and its argument(s)
     * @return (potentially complex) {@code Predicate} that encodes the time stamp function. Atoms are time stamp comparisons
     */
    private Predicate createStampPredicates(TimePoint tp, GDLParser.StampFuncContext stampFunc) {
        if (stampFunc.beforePointOperator() != null) {
            return createBeforePredicates(tp, stampFunc.beforePointOperator());
        } else if (stampFunc.afterPointOperator() != null) {
            return createAfterPredicates(tp, stampFunc.afterPointOperator());
        } else if (stampFunc.precedesOperator() != null) {
            return createPrecedesPredicates(tp, stampFunc.precedesOperator());
        } else if (stampFunc.succeedsOperator() != null) {
            return createSucceedsPredicates(tp, stampFunc.succeedsOperator());
        }
        return null;
    }


    /**
     * Creates a before {@code Predicate} given the caller (a timestamp) and its context
     *
     * @param from the caller
     * @param ctx  its context including the argument
     * @return a {@code Predicate} encoding the before function: from<x
     */
    private Predicate createBeforePredicates(TimePoint from, GDLParser.BeforePointOperatorContext ctx) {
        TimePoint x = buildTimePoint(ctx.timePoint());
        return new Comparison(from, Comparator.LT, x);
    }

    /**
     * Creates a after {@code Predicate} given the caller (a timestamp) and its context
     *
     * @param from the caller
     * @param ctx  context including the argument
     * @return a {@code Predicate} encoding the after function: from>x
     */
    private Predicate createAfterPredicates(TimePoint from, GDLParser.AfterPointOperatorContext ctx) {
        TimePoint x = buildTimePoint(ctx.timePoint());
        return new Comparison(from, Comparator.GT, x);
    }

    /**
     * Create asOf predicate: a.tx_from<=point AND a.tx_to>= point
     * @param ctx asOf context
     * @return asOf predicate
     */
    Predicate createAsOf(GDLParser.AsOfContext ctx) {
        TimePoint tp = buildTimePoint(ctx.timePoint());
        String identifier = loader.resolveIdentifier(ctx.Identifier().getText());
        return new And(
                        new Comparison(
                                new TimeSelector(identifier, TimeSelector.TimeField.TX_FROM),
                                LTE,
                                tp),
                        new Comparison(
                                new TimeSelector(identifier, TimeSelector.TimeField.TX_TO),
                                GTE,
                                tp)

        );
    }
}

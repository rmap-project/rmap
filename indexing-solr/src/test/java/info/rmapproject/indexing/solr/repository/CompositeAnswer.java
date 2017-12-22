package info.rmapproject.indexing.solr.repository;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static info.rmapproject.indexing.IndexUtils.assertNull;
import static info.rmapproject.indexing.IndexUtils.ise;
import static org.junit.Assert.fail;

/**
 * Provides a mechanism for chaining multiple {@link Answer}s and reducing the response from the {@code Answer} chain
 * to a single return value.
 * <p>
 * An answer chain is simply a {@code List<Answer>} that are invoked in order when {@link #answer(InvocationOnMock)} is
 * invoked by the caller.
 * </p>
 *
 * @param <T> the type returned by {@link #answer(InvocationOnMock)}
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
class CompositeAnswer<T> implements Answer<T> {

    /**
     * The chain of answers that are built up by the caller, then invoked.  The return value of each answer is either
     * collected by the {@link #answerCollector}, or provided by the {@link #answerSupplier}.
     */
    private List<TraceableAnswer<T>> answerChain;

    /**
     * If not {@code null}, used to reduce the responses from the {@link #answerChain}.  Mutually exclusive to
     * {@link #answerSupplier}.
     */
    private Collector<T, ?, T> answerCollector;

    /**
     * If not {@code null}, used to provide the respone from th {@link #answerChain}.  Mutually exclusive to
     * {@link #answerCollector}.
     */
    private Supplier<T> answerSupplier;

    /**
     * Instantiates a new {@code CompositeAnswer} with an empty answer chain.
     */
    CompositeAnswer() {
        this.answerChain = new ArrayList<>();
    }

    /**
     * Instantiates a new {@code CompositeAnswer} with the supplied answer chain.
     *
     * @param answerChain the answer chain
     */
    CompositeAnswer(List<Answer<T>> answerChain) {
        this.answerChain = answerChain.stream().map(answer -> new TraceableAnswer<>(answer, new Throwable())).collect(Collectors.toList());
    }

    /**
     * Adds an Answer to this Composite answer.  The result of the supplied {@code answer} may be
     * {@link #collectWith(Collector) collected} to inform the final result of this {@link #answer(InvocationOnMock)}.
     *
     * @param answer the answer
     * @return this composite answer
     */
    CompositeAnswer<T> andAnswer(Answer<T> answer) {
        answerChain.add(new TraceableAnswer<>(answer, new Throwable()));
        return this;
    }

    /**
     * Perform a reduction on the responses collected from each Answer managed by this composite answer.  The result of
     * the reduction will be the {@link #answer(InvocationOnMock) response} from this Answer.
     * <p>
     * Mutually exclusive with {@link #answerWith(Supplier)}.
     * </p>
     *
     * @param collector performs a mutable reduction on the stream of answers
     * @param <A> the accumulator type
     * @return this composite answer
     * @throws IllegalStateException if an {@link #answerWith(Supplier)}  Answer Supplier} has already been provided
     */
    <A> CompositeAnswer<T> collectWith(Collector<T, A, T> collector) {
        assertNull(this.answerSupplier,
                ise("Can only invoke one of 'andAnswer(Answer)' or 'collectWith(Collector)'.  " +
                        "'andAnswer(Answer)' has already been invoked."));
        this.answerCollector = collector;
        return this;
    }

    /**
     * The {@link #answer(InvocationOnMock) response} of this composite answer will be provided by the
     * {@code answerSupplier}.
     * <p>
     * Mutually exclusive with {@link #collectWith(Collector)}.
     * </p>
     *
     * @param answerSupplier the answer used by this composite answer
     * @return this composite answer
     * @throws IllegalStateException if a {@link #collectWith(Collector) collector} has already been supplied
     */
    CompositeAnswer<T> answerWith(Supplier<T> answerSupplier) {
        assertNull(this.answerCollector,
                ise("Can only invoke one of 'andAnswer(Answer)' or 'collectWith(Collector)'.  " +
                        "'collectWith(Collector)' has already been invoked."));
        this.answerSupplier = answerSupplier;
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Invokes each answer provided by {@link #andAnswer(Answer)} on the supplied {@code invocationOnMock}.
     * </p>
     * <p>
     * If an answer {@code Supplier} was {@link #answerWith(Supplier) provided} by the caller, the result of the
     * underlying answers are ignored, and the answer object provided {@code Supplier} is returned. If a {@code
     * Collector} was {@link #collectWith(Collector) supplied}, the {@code Collector} is invoked to provide a return.
     * If neither a {@code Collector} or {@code Supplier} are provided, {@code null} is returned.
     * </p>
     *
     * @param invocationOnMock {@inheritDoc}
     * @return the answer object returned by the {@code Supplier}, the result of the {@code Collector} (if supplied), or
     *         {@code null}
     * @throws Throwable {@inheritDoc}
     */
    @Override
    public T answer(InvocationOnMock invocationOnMock) throws Throwable {
        Stream.Builder<T> answerStream = Stream.builder();
        answerChain.forEach(answer -> {
            try {
                answerStream.accept(answer.answer(invocationOnMock));
            } catch (AssertionError assertionError) {
                String msg = String.format("Assertion failure: %s\n%s\nAssertion set from:\n%s",
                        assertionError.getMessage(), collectTrace(assertionError), answer.trace());
                fail(msg);
            } catch (Throwable throwable) {
                String msg = String.format("Unexpected exception: %s\n%s\nAssertion set from:\n%s",
                        throwable.getMessage(), collectTrace(throwable), answer.trace());
                fail(msg);
            }
        });

        if (answerCollector != null) {
            return answerStream.build().collect(answerCollector);
        }

        if (answerSupplier != null) {
            return answerSupplier.get();
        }

        return null;
    }

    /**
     * An {@code Answer} that keeps track of where it was set from.
     *
     * @param <T> the return type of {@code Answer}
     */
    private class TraceableAnswer<T> implements Answer<T> {
        private Answer<T> delegate;
        private Throwable trace;

        TraceableAnswer(Answer<T> delegate, Throwable trace) {
            this.delegate = delegate;
            this.trace = trace;
        }

        @Override
        public T answer(InvocationOnMock invocationOnMock) throws Throwable {
            return delegate.answer(invocationOnMock);
        }

        String trace() {
            return collectTrace(trace);
        }
    }

    private static String collectTrace(Throwable t) {
        try (ByteArrayOutputStream trace = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(trace, true)) {
            t.printStackTrace(ps);
            return new String(trace.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}

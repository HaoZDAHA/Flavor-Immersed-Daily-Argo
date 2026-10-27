package net.minecraft.network.codec;

import java.util.Collection;
import java.util.function.Function;

// 1.21 的 StreamCodec 在 1.20.1 里没有。这里保留同样的用法：
// composite / of / unit / map / apply，内部一律按网络缓冲区处理。
// B 只是一个形式参数，实际传入的都是 ByteBuf / FriendlyByteBuf。
public interface StreamCodec<B, V> {

    void encode(B buf, V value);

    V decode(B buf);

    default <O> StreamCodec<B, O> map(Function<? super V, ? extends O> toOuter,
                                      Function<? super O, ? extends V> toInner) {
        StreamCodec<B, V> self = this;
        return StreamCodec.of((buf, value) -> self.encode(buf, toInner.apply(value)),
                buf -> toOuter.apply(self.decode(buf)));
    }

    default <C extends Collection<V>> StreamCodec<B, C> apply(Collector<V, C> collector) {
        StreamCodec<B, V> self = this;
        return StreamCodec.of((buf, list) -> {
            net.minecraft.network.FriendlyByteBuf out = (net.minecraft.network.FriendlyByteBuf) buf;
            out.writeVarInt(list.size());
            for (V value : list) {
                self.encode(buf, value);
            }
        }, buf -> {
            net.minecraft.network.FriendlyByteBuf in = (net.minecraft.network.FriendlyByteBuf) buf;
            int size = in.readVarInt();
            C list = collector.create(size);
            for (int i = 0; i < size; i++) {
                list.add(self.decode(buf));
            }
            return list;
        });
    }

    static <B, V> StreamCodec<B, V> of(StreamEncoder<B, V> encoder, StreamDecoder<B, V> decoder) {
        return new StreamCodec<>() {
            @Override
            public void encode(B buf, V value) {
                encoder.encode(buf, value);
            }

            @Override
            public V decode(B buf) {
                return decoder.decode(buf);
            }
        };
    }

    static <B, V> StreamCodec<B, V> unit(V value) {
        return of((buf, ignored) -> {
        }, buf -> value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <B, C, T1> StreamCodec<B, C> composite(StreamCodec<?, T1> c1, Function<C, T1> g1,
                                                 Function<T1, C> constructor) {
        StreamCodec raw1 = c1;
        return of((buf, value) -> raw1.encode(buf, g1.apply(value)),
                buf -> constructor.apply((T1) raw1.decode(buf)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <B, C, T1, T2> StreamCodec<B, C> composite(StreamCodec<?, T1> c1, Function<C, T1> g1,
                                                      StreamCodec<?, T2> c2, Function<C, T2> g2,
                                                      java.util.function.BiFunction<T1, T2, C> constructor) {
        StreamCodec r1 = c1;
        StreamCodec r2 = c2;
        return of((buf, value) -> {
            r1.encode(buf, g1.apply(value));
            r2.encode(buf, g2.apply(value));
        }, buf -> constructor.apply((T1) r1.decode(buf), (T2) r2.decode(buf)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <B, C, T1, T2, T3> StreamCodec<B, C> composite(StreamCodec<?, T1> c1, Function<C, T1> g1,
                                                          StreamCodec<?, T2> c2, Function<C, T2> g2,
                                                          StreamCodec<?, T3> c3, Function<C, T3> g3,
                                                          Constructor3<T1, T2, T3, C> constructor) {
        StreamCodec r1 = c1;
        StreamCodec r2 = c2;
        StreamCodec r3 = c3;
        return of((buf, value) -> {
            r1.encode(buf, g1.apply(value));
            r2.encode(buf, g2.apply(value));
            r3.encode(buf, g3.apply(value));
        }, buf -> constructor.apply((T1) r1.decode(buf), (T2) r2.decode(buf), (T3) r3.decode(buf)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <B, C, T1, T2, T3, T4> StreamCodec<B, C> composite(StreamCodec<?, T1> c1, Function<C, T1> g1,
                                                              StreamCodec<?, T2> c2, Function<C, T2> g2,
                                                              StreamCodec<?, T3> c3, Function<C, T3> g3,
                                                              StreamCodec<?, T4> c4, Function<C, T4> g4,
                                                              Constructor4<T1, T2, T3, T4, C> constructor) {
        StreamCodec r1 = c1;
        StreamCodec r2 = c2;
        StreamCodec r3 = c3;
        StreamCodec r4 = c4;
        return of((buf, value) -> {
            r1.encode(buf, g1.apply(value));
            r2.encode(buf, g2.apply(value));
            r3.encode(buf, g3.apply(value));
            r4.encode(buf, g4.apply(value));
        }, buf -> constructor.apply((T1) r1.decode(buf), (T2) r2.decode(buf),
                (T3) r3.decode(buf), (T4) r4.decode(buf)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <B, C, T1, T2, T3, T4, T5> StreamCodec<B, C> composite(StreamCodec<?, T1> c1, Function<C, T1> g1,
                                                                  StreamCodec<?, T2> c2, Function<C, T2> g2,
                                                                  StreamCodec<?, T3> c3, Function<C, T3> g3,
                                                                  StreamCodec<?, T4> c4, Function<C, T4> g4,
                                                                  StreamCodec<?, T5> c5, Function<C, T5> g5,
                                                                  Constructor5<T1, T2, T3, T4, T5, C> constructor) {
        StreamCodec r1 = c1;
        StreamCodec r2 = c2;
        StreamCodec r3 = c3;
        StreamCodec r4 = c4;
        StreamCodec r5 = c5;
        return of((buf, value) -> {
            r1.encode(buf, g1.apply(value));
            r2.encode(buf, g2.apply(value));
            r3.encode(buf, g3.apply(value));
            r4.encode(buf, g4.apply(value));
            r5.encode(buf, g5.apply(value));
        }, buf -> constructor.apply((T1) r1.decode(buf), (T2) r2.decode(buf), (T3) r3.decode(buf),
                (T4) r4.decode(buf), (T5) r5.decode(buf)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <B, C, T1, T2, T3, T4, T5, T6> StreamCodec<B, C> composite(StreamCodec<?, T1> c1, Function<C, T1> g1,
                                                                      StreamCodec<?, T2> c2, Function<C, T2> g2,
                                                                      StreamCodec<?, T3> c3, Function<C, T3> g3,
                                                                      StreamCodec<?, T4> c4, Function<C, T4> g4,
                                                                      StreamCodec<?, T5> c5, Function<C, T5> g5,
                                                                      StreamCodec<?, T6> c6, Function<C, T6> g6,
                                                                      Constructor6<T1, T2, T3, T4, T5, T6, C> constructor) {
        StreamCodec r1 = c1;
        StreamCodec r2 = c2;
        StreamCodec r3 = c3;
        StreamCodec r4 = c4;
        StreamCodec r5 = c5;
        StreamCodec r6 = c6;
        return of((buf, value) -> {
            r1.encode(buf, g1.apply(value));
            r2.encode(buf, g2.apply(value));
            r3.encode(buf, g3.apply(value));
            r4.encode(buf, g4.apply(value));
            r5.encode(buf, g5.apply(value));
            r6.encode(buf, g6.apply(value));
        }, buf -> constructor.apply((T1) r1.decode(buf), (T2) r2.decode(buf), (T3) r3.decode(buf),
                (T4) r4.decode(buf), (T5) r5.decode(buf), (T6) r6.decode(buf)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> composite(
            StreamCodec<?, T1> c1, Function<C, T1> g1,
            StreamCodec<?, T2> c2, Function<C, T2> g2,
            StreamCodec<?, T3> c3, Function<C, T3> g3,
            StreamCodec<?, T4> c4, Function<C, T4> g4,
            StreamCodec<?, T5> c5, Function<C, T5> g5,
            StreamCodec<?, T6> c6, Function<C, T6> g6,
            StreamCodec<?, T7> c7, Function<C, T7> g7,
            Constructor7<T1, T2, T3, T4, T5, T6, T7, C> constructor) {
        StreamCodec r1 = c1;
        StreamCodec r2 = c2;
        StreamCodec r3 = c3;
        StreamCodec r4 = c4;
        StreamCodec r5 = c5;
        StreamCodec r6 = c6;
        StreamCodec r7 = c7;
        return of((buf, value) -> {
            r1.encode(buf, g1.apply(value));
            r2.encode(buf, g2.apply(value));
            r3.encode(buf, g3.apply(value));
            r4.encode(buf, g4.apply(value));
            r5.encode(buf, g5.apply(value));
            r6.encode(buf, g6.apply(value));
            r7.encode(buf, g7.apply(value));
        }, buf -> constructor.apply((T1) r1.decode(buf), (T2) r2.decode(buf), (T3) r3.decode(buf),
                (T4) r4.decode(buf), (T5) r5.decode(buf), (T6) r6.decode(buf), (T7) r7.decode(buf)));
    }

    public interface StreamEncoder<B, V> {
        void encode(B buf, V value);
    }

    public interface StreamDecoder<B, V> {
        V decode(B buf);
    }

    public interface Collector<T, C extends Collection<T>> {
        C create(int size);
    }

    public interface Constructor3<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    public interface Constructor4<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }

    public interface Constructor5<A, B, C, D, E, R> {
        R apply(A a, B b, C c, D d, E e);
    }

    public interface Constructor6<A, B, C, D, E, F, R> {
        R apply(A a, B b, C c, D d, E e, F f);
    }

    public interface Constructor7<A, B, C, D, E, F, G, R> {
        R apply(A a, B b, C c, D d, E e, F f, G g);
    }
}

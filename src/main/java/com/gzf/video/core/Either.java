/*
 * Copyright (c) 2017  Six Edge.
 *
 * This Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *               http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.gzf.video.core;

import java.util.function.Function;

/**
 * <h5>Left or Right</h5>
 * Lazy, functional, thread-safe Either implementation.
 * Usually, Left represents wrong, Right represents ok.
 */
public interface Either<L, R> {

    default boolean isLeft() { return false; }
    default boolean isRight() { return false; }

    default L getLeft() { return null; }
    default R getRight() { return null; }


    @FunctionalInterface
    interface Left<L, R> extends Either<L, R> {

        L getLeft();

        default boolean isLeft() { return true; }

    }


    @FunctionalInterface
    interface Right<L, R> extends Either<L, R> {

        R getRight();

        default boolean isRight() { return true; }

    }



    /**
     * Just for fun (but thread safe) ,
     * <pre>Either L R -> (R -> RR) -> Either L RR</pre>
     *
     * @param f pure function required
     * @return a new instant of Right
     */
    default <RR> Either<L, RR> fmap(Function<R, RR> f) {
        if (this.isLeft()) {
            return (Left<L, RR>) this;
        }

        return (Right<L, RR>) (() -> f.apply(this.getRight()));
    }

    /**
     * Just for fun (but thread safe) ,
     * <pre>Either L (R -> RR) -> Either L R -> Either L RR</pre>
     */
    @SuppressWarnings("unchecked")
    default <RR> Either<L, RR> applicative(Either<L, R> a) {
        if (this.isLeft()) {
            return (Left<L, RR>) this;
        }

        return a.fmap((Function<R, RR>) this.getRight());
    }

    /**
     * Just for fun (but thread safe) ,
     * <pre>Either L R -> (R -> Either L RR) -> Either L RR</pre>
     */
    default <RR> Either<L, RR> bind(Function<R, Either<L, RR>> f) {
        if (this.isLeft()) {
            return (Left<L, RR>) this;
        }

        return f.apply(this.getRight());
    }

    /**
     * <pre>Either L R -> (L -> C) -> (R -> C) -> C</pre>
     */
    default <C> C either(Function<L, C> lf, Function<R, C> rf) {
        return this.isRight()
                ? rf.apply(this.getRight())
                : lf.apply(this.getLeft());
    }

//    =============================================================

    /**
     * Just for fun (but thread safe) ,
     * <pre>Either L (T -> Either L RR) -> Either L T -> Either L RR</pre>
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    default <RR, T> Either<L, RR> wrapperBindWrapper(Either<L, T> a) {
        if (this.isLeft()) {
            return (Left<L, RR>) this;
        } else if (a.isLeft()) {
            return (Left<L, RR>) a;
        }

        return ((Function<T, Either<L, RR>>) this.getRight()).apply(a.getRight());
    }

    /**
     * Just for fun (but thread safe) ,
     * <pre>Either L (T -> Either L RR) -> T -> Either L RR</pre>
     */
    @SuppressWarnings("unchecked")
    default <RR, T> Either<L, RR> wrapperBind(T r) {
        if (this.isLeft()) {
            return (Left<L, RR>) this;
        }

        return ((Function<T, Either<L, RR>>) this.getRight()).apply(r);
    }
}

package com.muedsa.util

fun <T> Iterable<T>.anyMatchWithIndex(predicate: (Int, T) -> Boolean): Boolean {
    for ((i, e) in this.withIndex()) if (predicate(i, e)) return true
    return false
}
package com.josealfonsomora.firebasephonenumberauth

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.disposeWith(disposables: CompositeDisposable): Disposable {
    disposables.add(this)
    return this
}

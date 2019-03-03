package io.cassaundra.rocket

interface LaunchpadScanner {
	/**
	 * Begin scanning for the [LaunchpadClient].
	 */
	fun beginScan(onSuccess: Runnable)

	/**
	 * Do a single scan synchronously.
	 */
	fun quickScan(onSuccess: Runnable)

	/**
	 * Stop scanning. Should block until stopped.
	 */
	fun stopScan()
}
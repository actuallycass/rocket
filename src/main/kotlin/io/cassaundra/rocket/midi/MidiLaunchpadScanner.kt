package io.cassaundra.rocket.midi

import io.cassaundra.rocket.LaunchpadClient
import io.cassaundra.rocket.LaunchpadScanner
import io.cassaundra.rocket.Rocket
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import javax.sound.midi.MidiUnavailableException

class MidiLaunchpadScanner(val scanRateMillis: Long) : LaunchpadScanner {
	private val logger = LoggerFactory.getLogger(MidiLaunchpadScanner::class.java)

	private val threadPool = Executors.newSingleThreadExecutor()
	private val dispatcher = threadPool.asCoroutineDispatcher()

	private var job: Job? = null

	override fun beginScan(onSuccess: Runnable) {
		if(job != null && job?.isActive!!)
			throw IllegalStateException("beginScan was called while already scanning")

		job = GlobalScope.launch(dispatcher) {
			while(job!!.isActive) {
				scan(onSuccess)
				delay(scanRateMillis)
			}
		}
	}

	override fun quickScan(onSuccess: java.lang.Runnable) = runBlocking {
		scan(onSuccess)
	}

	private suspend fun scan(onSuccess: Runnable) = coroutineScope {
		val config = MidiDeviceConfiguration.autodetect()

		if(config.inputDevice == null || config.outputDevice == null) {
			Rocket.client = null

		} else if(Rocket.client == null || Rocket.client !is LaunchpadClient) {
			try {
				Rocket.client = MidiLaunchpadClient(config)
				onSuccess.run()
			} catch(exc: MidiUnavailableException) {
				logger.error("Could not setup MIDI launchpad", exc)
			}
		}
	}

	override fun stopScan() = runBlocking {
		if(job != null && job!!.isActive)
			job!!.cancelAndJoin()
	}
}
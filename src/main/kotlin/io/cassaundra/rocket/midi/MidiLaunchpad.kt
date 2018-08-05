package io.cassaundra.rocket.midi

import io.cassaundra.rocket.*
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import javax.sound.midi.*

internal class MidiLaunchpad @Throws(MidiUnavailableException::class)
constructor(private val configuration: MidiDeviceConfiguration?) : LaunchpadClient {
	private val logger = LoggerFactory.getLogger(MidiLaunchpad::class.java)

	private val receiver: Receiver? // LP -> Rocket
	private val transmitter: Transmitter? // Rocket -> LP

	private var onTextComplete: Runnable = Runnable {  }

	init {
		// setup output device
		val outputDevice = configuration?.outputDevice

		if(outputDevice?.isOpen == false) outputDevice.open()
		receiver = outputDevice?.receiver

		// setup input device
		val inputDevice = configuration?.inputDevice

		if(inputDevice?.isOpen == false) inputDevice.open()
		transmitter = inputDevice?.transmitter
	}

	override fun close() {
		if (configuration == null)
			return

		if(configuration.outputDevice?.isOpen == true)
			configuration.outputDevice.close()

		if(configuration.inputDevice?.isOpen == true)
			configuration.inputDevice.close()
	}

	override fun setListener(listener: LaunchpadListener) {
		if (transmitter != null)
			transmitter.receiver = MidiLaunchpadReceiver(listener)
	}

	override fun sendPadColor(pad: Pad, color: Color) {
		try {
			sendLEDChange(getNote(pad), color)
		} catch (e: InvalidMidiDataException) {
			e.printStackTrace()
		}

	}

	override fun sendButtonColor(button: Button, color: Color) {
		try {
			if (button.isTop) {
				sendLEDChange(button.coord + 104, color)
			} else {
				sendLEDChange((7 - button.coord) * 10 + 19, color)
			}
		} catch (e: InvalidMidiDataException) {
			logger.error("Invalid MIDI data", e)
		}
	}

	override fun clear() {
		clearAllLEDs()
	}

	override fun sendAllPadColors(color: Color) {
		for (y in 0..7) {
			for (x in 0..7) {
				sendPadColor(Pad(x, y), color)
			}
		}
	}

	private fun clearAllLEDs() {
		sendSysExMessage(byteArrayOf(240.toByte(), 0.toByte(), 32.toByte(), 41.toByte(), 2.toByte(), 24.toByte(), 14.toByte(), 0.toByte(), 247.toByte()))
	}

	private fun sendLEDChange(note: Int, color: Color) {
		sendSysExMessage(byteArrayOf(240.toByte(), 0.toByte(), 32.toByte(), 41.toByte(), 2.toByte(), 24.toByte(), 11.toByte(), note.toByte(), color.red.toByte(), color.green.toByte(), color.blue.toByte(), 247.toByte()))
	}

	override fun displayText(text: String, color: Color, onComplete: Runnable) {
		onTextComplete = onComplete
		var bytes = byteArrayOf(240.toByte(), 0.toByte(), 32.toByte(), 41.toByte(), 2.toByte(), 24.toByte(), 20.toByte(), 3.toByte(), 0.toByte())
		bytes += text.toByteArray(StandardCharsets.US_ASCII)
		bytes += 247.toByte()

		sendSysExMessage(bytes)
	}

	// midi utils

	@Throws(InvalidMidiDataException::class)
	private fun sendShortMessage(command: Int, channel: Int, controller: Int, data: Int) {
		send(ShortMessage(command, channel, controller, data))
	}

	@Throws(InvalidMidiDataException::class)
	private fun sendSysExMessage(data: ByteArray) {
		send(SysexMessage(data, data.size))
	}


	private fun send(message: MidiMessage) {
		receiver?.send(message, -1)
	}

	private fun getNote(pad: Pad): Int {
		return 11 + pad.x + pad.y * 10
	}

	// listening

	inner class MidiLaunchpadReceiver(var launchpadListener: LaunchpadListener) : Receiver {
		override fun send(message: MidiMessage, timestamp: Long) = when (message) {
			is ShortMessage ->
				handleShortMessage(message)
			is SysexMessage -> {
				onTextComplete.run()
				onTextComplete = Runnable { }
			}
			else -> throw RuntimeException("Unknown event: $message")
		}

		private fun handleShortMessage(message: ShortMessage) {
			val status = message.status
			val note = message.data1
			val velocity = message.data2

			when (status) {
				ShortMessage.NOTE_ON -> handleNoteOnMessage(note, velocity)
				ShortMessage.CONTROL_CHANGE -> handleControlChangeMessage(note, velocity)
				else -> throw RuntimeException("Unknown event: $message")
			}
		}

		private fun handleNoteOnMessage(note: Int, velocity: Int) {
			val pad: Pad? = Pad.fromMidi(note)
			if (pad != null) {
				if (velocity == 0)
					launchpadListener.onPadUp(pad)
				else
					launchpadListener.onPadDown(pad)
			} else {
				val button = Button.fromMidiRight(note)!!
				if (velocity == 0)
					launchpadListener.onButtonUp(button)
				else
					launchpadListener.onButtonDown(button)
			}
		}

		private fun handleControlChangeMessage(note: Int, velocity: Int) {
			val button = Button.fromMidiTop(note)
			if (velocity == 0) {
				launchpadListener.onButtonUp(button)
			} else {
				launchpadListener.onButtonDown(button)
			}
		}

		override fun close() {}
	}
}
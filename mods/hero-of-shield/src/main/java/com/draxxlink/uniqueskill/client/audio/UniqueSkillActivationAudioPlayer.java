package com.draxxlink.uniqueskill.client.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.io.BufferedInputStream;
import java.io.InputStream;

public final class UniqueSkillActivationAudioPlayer {
    private static final String ACTIVATION_AUDIO_PATH = "/assets/unique_skill/sounds/activation.mp3";

    private UniqueSkillActivationAudioPlayer() {
    }

    public static void play(float volume) {
        Thread playbackThread = new Thread(() -> playInternal(volume), "unique_skill-activation-audio");
        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    private static void playInternal(float volume) {
        try (InputStream resourceStream = UniqueSkillActivationAudioPlayer.class.getResourceAsStream(ACTIVATION_AUDIO_PATH)) {
            if (resourceStream == null) {
                System.err.println("[Habilidade Unica] Activation audio not found at " + ACTIVATION_AUDIO_PATH);
                return;
            }

            try (
                BufferedInputStream bufferedStream = new BufferedInputStream(resourceStream);
                AudioInputStream encodedStream = AudioSystem.getAudioInputStream(bufferedStream)
            ) {
                AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    encodedStream.getFormat().getSampleRate(),
                    16,
                    encodedStream.getFormat().getChannels(),
                    encodedStream.getFormat().getChannels() * 2,
                    encodedStream.getFormat().getSampleRate(),
                    false
                );

                try (AudioInputStream decodedStream = AudioSystem.getAudioInputStream(decodedFormat, encodedStream)) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(decodedStream);
                    applyVolume(clip, volume);
                    clip.addLineListener(event -> closeOnStop(clip, event));
                    clip.start();
                }
            }
        } catch (Exception exception) {
            System.err.println("[Habilidade Unica] Failed to play activation audio: " + exception.getMessage());
        }
    }

    private static void applyVolume(Clip clip, float volume) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float clampedVolume = Math.max(0.0F, Math.min(1.0F, volume));
        if (clampedVolume <= 0.0F) {
            gainControl.setValue(gainControl.getMinimum());
            return;
        }

        float decibels = (float) (20.0D * Math.log10(clampedVolume));
        gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(decibels, gainControl.getMaximum())));
    }

    private static void closeOnStop(Clip clip, LineEvent event) {
        if (event.getType() == LineEvent.Type.STOP) {
            clip.close();
        }
    }
}

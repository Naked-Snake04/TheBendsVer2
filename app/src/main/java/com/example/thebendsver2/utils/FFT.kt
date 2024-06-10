package com.example.thebendsver2.utils

import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.sqrt

class FFT {

    companion object {
        fun calculateFrequency(buffer: ShortArray, read: Int): Any {
            val fft = DoubleArray(read)
            for (i in buffer.indices) {
                fft[i] = buffer[i].toDouble()
            }
            // Выполнение БПФ (Быстрое преобразование Фурье)
            return performFFT(fft)
        }

        private fun performFFT(fft: DoubleArray): Double {
            val fftSize = fft.size

            // Преборазование данных для FFT
            val fftData = DoubleArray(fftSize * 2)
            for (i in fft.indices) {
                fftData[i * 2] = fft[i] // Реальная часть
                fftData[i * 2 + 1] = 0.0 // Мнимая часть
            }

            // Выполнение FFT
            val fftInstance = DoubleFFT_1D(fftSize.toLong())
            fftInstance.complexForward(fftData)

            // Вычисление амплитуд
            val magnitudes = DoubleArray(fftSize / 2)
            for (i in magnitudes.indices) {
                val real = fftData[2 * i]
                val imag = fftData[2 * i + 1]
                magnitudes[i] = sqrt(real * real + imag * imag)
            }

            // Нахождение частоты с наибольшй амплитудой
            var maxIndex = 0
            var maxMagnitude = magnitudes[0]
            for (i in 1 until magnitudes.size) {
                if (magnitudes[i] > maxMagnitude) {
                    maxMagnitude = magnitudes[i]
                    maxIndex = i
                }
            }

            // Вычисление частоты
            val sampleRate = 44100 // частота дискретизации
            return maxIndex.toDouble() * sampleRate / fftSize // Возвращаем частоту
        }
    }

}
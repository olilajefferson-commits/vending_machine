package com.mabini.vending;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

public class QRScanner {
    
    public static String scanQR(JFrame parent) {
        AtomicReference<String> result = new AtomicReference<>(null);
        JDialog dialog = new JDialog(parent, "Scan Mabini Student QR", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(320, 240);
        dialog.setLocationRelativeTo(parent);

        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            JOptionPane.showMessageDialog(parent, "No webcam detected!");
            return null;
        }
        
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setMirrored(true);

        JLabel status = new JLabel("Show your Student ID QR", SwingConstants.CENTER);
        status.setFont(new Font("Arial", Font.BOLD, 14));
        status.setForeground(Color.BLUE);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(status, BorderLayout.SOUTH);

        Thread scanThread = new Thread(() -> {
            try {
                webcam.open(); // Explicitly open
                while (result.get() == null && webcam.isOpen() && dialog.isVisible()) {
                    BufferedImage image = webcam.getImage();
                    if (image == null) {
                        Thread.sleep(100);
                        continue;
                    }

                    LuminanceSource source = new BufferedImageLuminanceSource(image);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                    try {
                        Result qrResult = new MultiFormatReader().decode(bitmap);
                        String scannedText = qrResult.getText().trim();

                        // VALIDATION: Must be exactly 6 digits
                        if (scannedText.matches("^\\d{6}$")) {
                            result.set(scannedText);
                            SwingUtilities.invokeLater(() -> {
                                status.setText("SUCCESS: Valid Student ID " + scannedText);
                                status.setForeground(new Color(0, 150, 0));
                            });
                            Thread.sleep(1200); // Show success
                            break; // Exit loop
                        } else {
                            // Invalid QR - show error but keep scanning
                            SwingUtilities.invokeLater(() -> {
                                status.setText("INVALID: Not a 6-digit ID. Got: " + scannedText);
                                status.setForeground(Color.RED);
                            });
                            Thread.sleep(2000);
                            SwingUtilities.invokeLater(() -> {
                                status.setText("Show your Student ID QR");
                                status.setForeground(Color.BLUE);
                            });
                        }
                    } catch (NotFoundException e) {
                        // No QR found, just continue
                    }
                    Thread.sleep(150); // Small delay to prevent CPU spike
                }
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> 
                    status.setText("Camera error: " + e.getMessage())
                );
            } finally {
                if (webcam.isOpen()) {
                    webcam.close();
                }
                SwingUtilities.invokeLater(dialog::dispose);
            }
        });
        
        scanThread.start();

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                scanThread.interrupt();
                if (webcam.isOpen()) {
                    webcam.close();
                }
            }
        });

        dialog.setVisible(true); // Blocks until dialog closes
        return result.get(); // Returns 6-digit ID or null
    }
}
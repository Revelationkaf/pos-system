package com.pos.service;

import com.pos.model.CartItem;
import com.pos.model.Sale;

import java.time.format.DateTimeFormatter;

public class ReceiptService {

    public static String generateReceiptNumber() {
        String timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int)(Math.random() * 9999);
        return "RCP-" + timestamp + "-" + String.format("%04d", random);
    }

    public static String generateReceiptText(Sale sale) {
        StringBuilder sb = new StringBuilder();
        String line = "-".repeat(50);
        String doubleLine = "=".repeat(50);

        sb.append(doubleLine).append("\n");
        sb.append(centerText("POINT OF SALE SYSTEM", 50)).append("\n");
        sb.append(centerText("123 Main Street, City", 50)).append("\n");
        sb.append(centerText("Tel: (555) 123-4567", 50)).append("\n");
        sb.append(doubleLine).append("\n");
        sb.append(String.format("%-25s %s\n", "Receipt #:", sale.getReceiptNumber()));
        sb.append(String.format("%-25s %s\n", "Date:", sale.getSaleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        sb.append(String.format("%-25s %s\n", "Cashier:", sale.getCashier()));
        sb.append(String.format("%-25s %s\n", "Payment Method:", sale.getPaymentMethod()));
        sb.append(line).append("\n");
        sb.append(String.format("%-20s %6s %10s %10s\n", "Item", "Qty", "Price", "Total"));
        sb.append(line).append("\n");

        for (CartItem item : sale.getItems()) {
            String name = item.getProduct().getName();
            if (name.length() > 18) {
                name = name.substring(0, 15) + "...";
            }
            sb.append(String.format("%-20s %6d %10.2f %10.2f\n",
                name,
                item.getQuantity(),
                item.getProduct().getPrice(),
                item.getSubtotal()));
        }

        sb.append(line).append("\n");
        sb.append(String.format("%-38s %10.2f\n", "Subtotal:", sale.getSubtotal()));
        sb.append(String.format("%-38s %10.2f\n", "Tax (8%):", sale.getTax()));
        if (sale.getDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-38s %10.2f\n", "Discount:", sale.getDiscount()));
        }
        sb.append(doubleLine).append("\n");
        sb.append(String.format("%-38s %10.2f\n", "TOTAL:", sale.getTotal()));
        sb.append(doubleLine).append("\n");
        sb.append(centerText("Thank you for your purchase!", 50)).append("\n");
        sb.append(centerText("Please come again", 50)).append("\n");
        sb.append(doubleLine).append("\n");

        return sb.toString();
    }

    private static String centerText(String text, int width) {
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        return " ".repeat(padding) + text;
    }
}

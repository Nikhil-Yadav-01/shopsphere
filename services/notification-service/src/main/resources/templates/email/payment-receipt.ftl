<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #28a745; color: white; padding: 20px; text-align: center; border-radius: 5px; }
        .content { padding: 20px; background-color: #f9f9f9; margin-top: 20px; border-radius: 5px; }
        .receipt-details { margin: 20px 0; }
        .detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #ddd; }
        .footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Payment Receipt</h1>
        </div>
        
        <div class="content">
            <p>Dear Valued Customer,</p>
            
            <p>Your payment has been successfully processed. Here are your receipt details:</p>
            
            <div class="receipt-details">
                <div class="detail-row">
                    <strong>Order ID:</strong>
                    <span>${orderId}</span>
                </div>
                <div class="detail-row">
                    <strong>Transaction ID:</strong>
                    <span>${transactionId}</span>
                </div>
                <div class="detail-row">
                    <strong>Amount Paid:</strong>
                    <span>$${amount}</span>
                </div>
                <div class="detail-row">
                    <strong>Payment Method:</strong>
                    <span>${paymentMethod}</span>
                </div>
                <div class="detail-row">
                    <strong>Confirmation Date:</strong>
                    <span>${confirmationDate}</span>
                </div>
            </div>
            
            <p>Your order will be packed and shipped soon. You'll receive a shipping notification with tracking information.</p>
            
            <p>Thank you for your business!</p>
        </div>
        
        <div class="footer">
            <p>&copy; 2025 ShopSphere. All rights reserved.</p>
        </div>
    </div>
</body>
</html>

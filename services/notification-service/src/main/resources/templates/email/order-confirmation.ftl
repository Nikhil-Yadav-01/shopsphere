<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #007bff; color: white; padding: 20px; text-align: center; border-radius: 5px; }
        .content { padding: 20px; background-color: #f9f9f9; margin-top: 20px; border-radius: 5px; }
        .order-details { margin: 20px 0; }
        .detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #ddd; }
        .footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }
        .button { display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; margin-top: 20px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Order Confirmation</h1>
        </div>
        
        <div class="content">
            <p>Dear Valued Customer,</p>
            
            <p>Thank you for your order! We're excited to process it for you.</p>
            
            <div class="order-details">
                <div class="detail-row">
                    <strong>Order ID:</strong>
                    <span>${orderId}</span>
                </div>
                <div class="detail-row">
                    <strong>Order Date:</strong>
                    <span>${orderDate}</span>
                </div>
                <div class="detail-row">
                    <strong>Order Total:</strong>
                    <span>$${totalAmount}</span>
                </div>
            </div>
            
            <p>You will receive another email once your order has shipped with tracking information.</p>
            
            <p>If you have any questions about your order, please contact our customer support team.</p>
            
            <a href="https://shop.example.com/orders/${orderId}" class="button">View Order Details</a>
        </div>
        
        <div class="footer">
            <p>&copy; 2025 ShopSphere. All rights reserved.</p>
            <p>This is an automated email. Please do not reply directly.</p>
        </div>
    </div>
</body>
</html>

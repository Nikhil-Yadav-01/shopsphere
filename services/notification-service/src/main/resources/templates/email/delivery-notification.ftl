<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #17a2b8; color: white; padding: 20px; text-align: center; border-radius: 5px; }
        .content { padding: 20px; background-color: #f9f9f9; margin-top: 20px; border-radius: 5px; }
        .delivery-details { margin: 20px 0; }
        .detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #ddd; }
        .footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }
        .button { display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; margin-top: 20px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Your Order Has Been Delivered!</h1>
        </div>
        
        <div class="content">
            <p>Your order has been successfully delivered!</p>
            
            <div class="delivery-details">
                <div class="detail-row">
                    <strong>Order ID:</strong>
                    <span>${orderId}</span>
                </div>
                <div class="detail-row">
                    <strong>Delivered To:</strong>
                    <span>${recipientName}</span>
                </div>
                <div class="detail-row">
                    <strong>Delivery Date:</strong>
                    <span>${deliveryDate}</span>
                </div>
            </div>
            
            <p>We hope you're happy with your purchase! If you have any questions or concerns about your order, please don't hesitate to contact us.</p>
            
            <p>We'd love to hear your feedback! Please consider leaving a review of your order.</p>
            
            <a href="https://shop.example.com/orders/${orderId}/review" class="button">Leave a Review</a>
        </div>
        
        <div class="footer">
            <p>&copy; 2025 ShopSphere. All rights reserved.</p>
        </div>
    </div>
</body>
</html>

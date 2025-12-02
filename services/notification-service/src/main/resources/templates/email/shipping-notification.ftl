<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #ffc107; color: black; padding: 20px; text-align: center; border-radius: 5px; }
        .content { padding: 20px; background-color: #f9f9f9; margin-top: 20px; border-radius: 5px; }
        .shipping-details { margin: 20px 0; }
        .detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #ddd; }
        .footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; color: #666; font-size: 12px; }
        .button { display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; margin-top: 20px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Your Order Has Shipped!</h1>
        </div>
        
        <div class="content">
            <p>Great news! Your order is on its way.</p>
            
            <div class="shipping-details">
                <div class="detail-row">
                    <strong>Order ID:</strong>
                    <span>${orderId}</span>
                </div>
                <div class="detail-row">
                    <strong>Tracking Number:</strong>
                    <span>${trackingNumber}</span>
                </div>
                <div class="detail-row">
                    <strong>Carrier:</strong>
                    <span>${carrier}</span>
                </div>
                <div class="detail-row">
                    <strong>Estimated Delivery:</strong>
                    <span>${estimatedDeliveryDate}</span>
                </div>
            </div>
            
            <p>You can track your shipment using the tracking number above on the carrier's website.</p>
            
            <a href="https://shop.example.com/orders/${orderId}/tracking" class="button">Track Your Order</a>
        </div>
        
        <div class="footer">
            <p>&copy; 2025 ShopSphere. All rights reserved.</p>
        </div>
    </div>
</body>
</html>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            color: #333;
        }

        .container {
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
        }

        .header {
            background-color: #6c757d;
            color: white;
            padding: 20px;
            text-align: center;
            border-radius: 5px;
        }

        .content {
            padding: 20px;
            background-color: #f9f9f9;
            margin-top: 20px;
            border-radius: 5px;
        }

        .return-details {
            margin: 20px 0;
        }

        .detail-row {
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
            border-bottom: 1px solid #ddd;
        }

        .footer {
            text-align: center;
            margin-top: 30px;
            padding-top: 20px;
            border-top: 1px solid #ddd;
            color: #666;
            font-size: 12px;
        }

        .button {
            display: inline-block;
            padding: 10px 20px;
            background-color: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin-top: 20px;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>Return Request Approved</h1>
    </div>

    <div class="content">
        <p>Good news! Your return request has been approved.</p>

        <div class="return-details">
            <div class="detail-row">
                <strong>RMA Number:</strong>
                <span>${rmaNumber}</span>
            </div>
            <div class="detail-row">
                <strong>Order ID:</strong>
                <span>${orderId}</span>
            </div>
            <div class="detail-row">
                <strong>Refund Amount:</strong>
                <span>$${refundAmount}</span>
            </div>
            <div class="detail-row">
                <strong>Approval Date:</strong>
                <span>${approvalDate}</span>
            </div>
        </div>

        <p>Please ship your return using the RMA number provided above. Once we receive and inspect your return, your
            refund will be processed within 5-7 business days.</p>

        <p><strong>Important:</strong> Please include the RMA number in your return package.</p>

        <a href="https://shop.example.com/returns/${rmaNumber}" class="button">View Return Details</a>
    </div>

    <div class="footer">
        <p>&copy; 2025 ShopSphere. All rights reserved.</p>
    </div>
</div>
</body>
</html>

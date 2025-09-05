<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Choose OTP Method</title>
    <style>
        body {
            font-family: "Segoe UI", sans-serif;
            background-color: #fff8f0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }

        .container {
            background: #fff;
            border: 2px solid #ffa726;
            border-radius: 12px;
            padding: 2rem 2.5rem;
            box-shadow: 0 6px 12px rgba(255, 167, 38, 0.2);
            text-align: center;
            width: 320px;
        }

        h2 {
            color: #fb8c00;
            margin-bottom: 1.5rem;
        }

        .btn {
            display: block;
            width: 100%;
            padding: 0.75rem;
            margin: 0.5rem 0;
            font-size: 1rem;
            font-weight: bold;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            background-color: #fb8c00;
            color: white;
            transition: background 0.3s ease;
        }

        .btn:hover {
            background-color: #ef6c00;
        }

        .note {
            font-size: 0.85rem;
            color: #666;
            margin-top: 1rem;
        }

        .error {
            color: red;
            margin-bottom: 1rem;
        }
    </style>
</head>
<body>
<div class="container">
    <h2>${msg("chooseOtpMethod")}</h2>

    <#if message?has_content>
        <div class="error">${message}</div>
    </#if>

    <form action="${url.loginAction}" method="post">
        <button class="btn" name="method" value="phone">${msg("sendOtpToPhone")}</button>
        <button class="btn" name="method" value="email">${msg("sendOtpToEmail")}</button>
    </form>

    <div class="note">${msg("otpMethodNote")}</div>
</div>
</body>
</html>

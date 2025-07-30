<!DOCTYPE html>
<html>
<head>
    <title>Phone Number Login</title>
    <style>
        body {
            font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
            background-color: #fff8f0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }

        .form-container {
            background-color: #ffffff;
            border: 1px solid #ffc107;
            border-radius: 12px;
            padding: 2rem;
            width: 320px;
            box-shadow: 0 0 12px rgba(255, 152, 0, 0.2);
        }

        h2 {
            text-align: center;
            color: #ff6f00;
            margin-bottom: 1.5rem;
        }

        label {
            display: block;
            margin-bottom: 0.5rem;
            color: #ff6f00;
            font-weight: 600;
        }

        input[type="text"] {
            width: 100%;
            padding: 0.6rem;
            border: 1px solid #ffcc80;
            border-radius: 6px;
            margin-bottom: 1rem;
            outline: none;
        }

        input[type="submit"] {
            background-color: #ff9800;
            color: white;
            border: none;
            padding: 0.7rem 1rem;
            border-radius: 6px;
            cursor: pointer;
            width: 100%;
            font-weight: bold;
            transition: background-color 0.2s ease-in-out;
        }

        input[type="submit"]:hover {
            background-color: #fb8c00;
        }

        .kc-error-message {
            color: red;
            margin-bottom: 1rem;
        }
    </style>
</head>
<body>
    <form class="form-container" action="${url.loginAction}" method="post">
        <h2>Phone Login</h2>

        <#if message?has_content>
            <div class="kc-error-message">${message.summary}</div>
        </#if>

        <label for="phone_number">Phone Number</label>
        <input type="text" id="phone_number" name="phone_number" placeholder="+91XXXXXXXXXX" required>

        <input type="submit" value="Send OTP">
    </form>
</body>
</html>

<!DOCTYPE html>
<html>
<head>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Enter OTP</title>
  <style>
    body {
      font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
      background-color: #fff8f0;
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      margin: 0;
      padding: 1rem;
    }

    .form-container {
      background-color: #ffffff;
      border: 1px solid #ffc107;
      border-radius: 12px;
      padding: 2rem;
      width: 100%;
      max-width: 360px;
      box-shadow: 0 0 12px rgba(255, 152, 0, 0.2);
      text-align: center;
    }

    h2 {
      color: #ff6f00;
      margin-bottom: 1.5rem;
      font-size: 1.5rem;
    }

    .otp-inputs {
      display: flex;
      justify-content: center;
      gap: 0.5rem;
      flex-wrap: wrap;
      margin-bottom: 1.5rem;
    }

    .otp-inputs input {
      width: 14%;
      min-width: 40px;
      height: 48px;
      font-size: 1.2rem;
      text-align: center;
      border: 1px solid #ffcc80;
      border-radius: 8px;
      outline: none;
      flex: 1 0 14%;
      box-sizing: border-box;
    }

    input[type="submit"],
    button[name="back"] {
      background-color: #ff9800;
      color: white;
      border: none;
      padding: 0.7rem 1rem;
      border-radius: 6px;
      cursor: pointer;
      width: 100%;
      font-weight: bold;
      transition: background-color 0.2s ease-in-out;
      margin-top: 0.5rem;
    }

    input[type="submit"]:hover,
    button[name="back"]:hover {
      background-color: #fb8c00;
    }

    .kc-error-message {
      color: red;
      margin-bottom: 1rem;
      font-size: 0.9rem;
    }

    @media (max-width: 480px) {
      .form-container {
        padding: 1.5rem 1rem;
      }
      .otp-inputs input {
        height: 42px;
        font-size: 1rem;
        flex: 1 0 20%; /* allow wrapping in 2 rows if needed */
      }
      h2 {
        font-size: 1.2rem;
      }
    }
  </style>
</head>
<body>
  <form class="form-container" id="otpForm" action="${url.loginAction}" method="post">
    <h2>Enter OTP</h2>

    <#if message?has_content>
      <div class="kc-error-message">${message.summary}</div>
    </#if>

    <div class="otp-inputs">
      <#list 1..6 as i>
        <input type="text" name="digit${i}" id="digit${i}" maxlength="1" inputmode="numeric" pattern="[0-9]*" required>
      </#list>
    </div>

    <input type="hidden" id="code" name="code" />

    <input type="submit" value="Verify OTP">
    <button type="submit" name="back" value="true">Go Back</button>
  </form>

  <script>
    const inputs = document.querySelectorAll('.otp-inputs input');
    const otpField = document.getElementById('code');
    const form = document.getElementById('otpForm');

    inputs.forEach((input, idx) => {
      input.addEventListener('input', () => {
        if (input.value.length === 1 && idx < inputs.length - 1) {
          inputs[idx + 1].focus();
        }
      });
      input.addEventListener('keydown', (e) => {
        if (e.key === "Backspace" && input.value === '' && idx > 0) {
          inputs[idx - 1].focus();
        }
      });
    });

    form.addEventListener('submit', () => {
      const otp = Array.from(inputs).map(i => i.value).join('');
      otpField.value = otp;
    });
  </script>
</body>
</html>
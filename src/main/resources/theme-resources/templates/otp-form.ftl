<!DOCTYPE html>
<html>
<head>
    <title>Enter OTP</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
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
            width: 340px;
            box-shadow: 0 0 12px rgba(255, 152, 0, 0.2);
            text-align: center;
        }

        h2 {
            color: #ff6f00;
            margin-bottom: 1.5rem;
        }

        .otp-inputs {
            display: flex;
            justify-content: space-between;
            gap: 0.5rem;
            margin-bottom: 1.5rem;
        }

        .otp-inputs input {
            width: 40px;
            height: 48px;
            font-size: 20px;
            text-align: center;
            border: 1px solid #ffcc80;
            border-radius: 8px;
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
        /* Responsive tweaks for small screens */
            @media (max-width: 480px) {
              .form-container {
                padding: 1.5rem 1rem;
                width: 100%;
              }
              .otp-inputs {
                    display: flex;
                    justify-content: space-between;
                    gap: 0.2rem;
                    margin-bottom: 1.5rem;
                }

                .otp-inputs input {
                    width: 17%;
                    height: 44px;
                    font-size: 16px;

                }

              h2 {
                font-size: 1.3rem;
                margin-bottom: 1rem;
              }

              label {
                font-size: 0.95rem;
              }

              input[type="text"],
              input[type="submit"] {
                font-size: 0.95rem;
                padding: 0.6rem;
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
        <div style="text-align: center;">
                 ${kcSanitize(msg("otpSentToYour",realmName, inputValue, ttl))?no_esc}
             </div>
        <div class="otp-inputs">
            <#list 1..6 as i>
                <input type="text" name="digit${i}" id="digit${i}" maxlength="1" inputmode="numeric" pattern="[0-9]*" required>
            </#list>
        </div>

        <!-- Hidden field to store combined OTP -->
        <input type="hidden" id="code" name="code" />

        <input type="submit" value="Verify OTP">
        <#if displayGoBack??>
           <button name="back" value="true">Change or correct the input ${kcSanitize(inputValue)?no_esc}</button>
        </#if>
        <#if isSimulation>
          <div style="text-align: center;">
                          ${kcSanitize(msg("otpSimulationModeDisplay",realmName, simulationOTP))?no_esc}
           </div>
        </#if>



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

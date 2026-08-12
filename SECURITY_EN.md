# Security

[Versao em portugues](SECURITY.md)

Before publishing any code in this repository, review whether it contains sensitive information.

## Never Publish

- API keys.
- Access tokens.
- Passwords.
- Cookies.
- `.env` files.
- Personal or client data.
- Sensitive local paths.

## Recommendation

Use `.env.example` files to show variable names without exposing real values.

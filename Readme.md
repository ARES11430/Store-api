# Spring Boot: Store API

In this project, we build the backend for an e-commerce application using Spring Boot. The API includes endpoints for:

- Managing products
- Managing shopping carts
- Checking out
- Viewing order history

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/ARES11430/Store-api.git
```

### 2. Configure Environment Variables
- Rename the ``.env.example`` file to ``.env``.
- Update the following environment variables inside .env:

#### JWT_SECRET

Generate a secure random key using:

```bash
openssl rand -base64 32
```

If ``openssl`` is not available, go to [generate-random.org](https://generate-random.org), click on **Strings > API Tokens**, and generate a secure token.

#### STRIPE_SECRET_KEY

- Create a free account at [stripe.com](https://stripe.com)
- On your dashboard, go to **Developers > API Keys**. You can use the search bar for quick access.
- Copy the value of the **Secret Key**.

#### STRIPE_WEBHOOK_SECRET_KEY

- Install the Stripe CLI: https://docs.stripe.com/stripe-cli
- Login and start the webhook listener:

```bash
stripe login
stripe listen --forward-to http://localhost:8080/checkout/webhook
```
- Copy the **signing secret** from the terminal output and use it as the value for ``STRIPE_WEBHOOK_SECRET_KEY``.

---

## ▶️ Running the Project

This is a Maven project. To start the application, run:

```bash
./mvnw spring-boot:run
```

If you're on Windows:

```bash
mvnw.cmd spring-boot:run
```

Once running, the application will be available at:

```arduino
http://localhost:8080
```

---

## 📚 API Documentation

Swagger UI is available at:

```bash
http://localhost:8080/swagger-ui.html
```

---

## 🧪 Example API Flow

Here's a sample flow to help you understand how to interact with the API after starting the application.

### 1. Get All Products

```bash
GET /products
```

The database is automatically populated with 10 sample products using a Flyway migration script.

### 2. Create a Shopping Cart

```bash
POST /carts
```

This will return the cart ID. You don't need to be logged in to create a cart.

### 3. Add Items to Cart

Once you have a cart ID, you can add products to it by sending:

```bash
POST /carts/{cartId}/items
```

**Request body example**:
```json
{
  "productId": 1
}
```

### 4. Register a New User
To check out, you have to register and login first:

```bash
POST /users
```

**Request body**:

```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "123456"
}
```

### 5. Login to Get an Access Token

```bash
POST /auth/login 
```

**Request body**:
```json
{
  "email": "john@example.com",
  "password": "123456"
}
```

**Response body**:
```json
{
  "token": "your-json-web-token"
}
```

### 6. Checkout

```bash
POST /checkout 
```

**Headers**
```bash
Authorization: Bearer your-json-web-token
```

**Request body**
```json
{
  "cartId": "your-cart-id"
}
```

This endpoint returns a Stripe checkout URL. Open it in your browser to complete the payment using a test card:

```yaml
Card: 4242 4242 4242 4242
Expiry: Any future date
CVC: Any 3 digits
```

### 7. Webhook & Order Status Update

Once payment is completed, Stripe will trigger a webhook call to:

```bash
POST /checkout/webhook 
```

Our backend listens for this event and updates the order status in the database accordingly.

---
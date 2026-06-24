# API Examples

Base URL: `http://localhost:8080/api/v1`

---

## 1. Register a factory + manager

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "managerName": "Ama Mensah",
    "phone": "0244000000",
    "password": "secret123",
    "factoryName": "Accra Textiles Ltd",
    "location": "Tema, Greater Accra",
    "industry": "Textiles"
  }'
```

Response:
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "8f3a...-...",
  "userId": "1f0c...",
  "factoryId": "9b22...",
  "name": "Ama Mensah",
  "role": "MANAGER"
}
```

Use `accessToken` as `Authorization: Bearer <token>` on every protected call.
The `factoryId` is baked into the token — you never send it yourself.

---

## 2. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "phone": "0244000000", "password": "secret123", "fcmToken": "device-token-optional" }'
```

---

## 3. Add a raw material

```bash
curl -X POST http://localhost:8080/api/v1/materials \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Cotton Fabric",
    "unit": "kg",
    "quantityInStock": 500,
    "reorderLevel": 100,
    "costPerUnit": 12.50
  }'
```

Response (note the auto-computed `lowStock`):
```json
{
  "materialId": "aa11...",
  "name": "Cotton Fabric",
  "unit": "kg",
  "quantityInStock": 500,
  "reorderLevel": 100,
  "costPerUnit": 12.50,
  "lowStock": false
}
```

---

## 4. Submit production (deducts stock, auto-calculates waste + cost)

```bash
curl -X POST http://localhost:8080/api/v1/production \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "School Shirts",
    "quantityProduced": 200,
    "shift": "Morning",
    "materialsUsed": [
      { "materialId": "aa11...", "quantityUsed": 80, "wasteAmount": 3.5 }
    ]
  }'
```

Response:
```json
{
  "entryId": "cc99...",
  "productName": "School Shirts",
  "quantityProduced": 200,
  "shift": "Morning",
  "entryDate": "2026-06-23",
  "locked": false,
  "workerId": "1f0c...",
  "workerName": "Ama Mensah",
  "totalMaterialUsed": 80,
  "totalWaste": 3.5,
  "estimatedMaterialCost": 1000.00,
  "materials": [
    { "materialId": "aa11...", "materialName": "Cotton Fabric", "quantityUsed": 80, "wasteAmount": 3.5 }
  ]
}
```

After this call the cotton stock is `500 - 80 = 420 kg`. If it had dropped to/below the reorder
level, a low-stock alert is logged (and will fire an FCM push once the notification slice is wired).

---

## 5. Multi-tenant isolation in action

If a user from **Factory A** tries to update a material that belongs to **Factory B**:

```bash
curl -X PUT http://localhost:8080/api/v1/materials/<factory-B-material-id> \
  -H "Authorization: Bearer $FACTORY_A_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{ "name": "hack", "unit": "kg", "quantityInStock": 0, "reorderLevel": 0, "costPerUnit": 0 }'
```

Response:
```json
{ "status": 404, "error": "Not Found", "message": "Material not found in this factory" }
```

The lookup is `findByMaterialIdAndFactory_FactoryId(id, tokenFactoryId)`, so Factory B's row is
invisible to Factory A — even with a valid ID.

---

## 6. Refresh tokens

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{ "refreshToken": "8f3a...-..." }'
```

The old refresh token is revoked and a new pair is issued (rotation).

# NTTData Test

### Input data

O input de dados é feito por uma conexão WebSocket no qual o cliente envia um objeto no padrão abaixo.
O `checksum` é um identificador único gerado pelo cliente para diferenciar cada ordem e prevenir que a mesma ordem seja
criada mais de uma vez.

```json
{
	"checksum": "{VALOR_DE_IDENTIFICADOR_UNICO}",
  "client": {
    "id": 213546879,
    "name": "Foo Bar",
    "taxId": "12345678900",
    "address": {
      "zipCode": "01310200",
      "streetName": "Avenida Paulista",
      "streetNumber": "1578",
      "neighborhood": "Bela Vista",
      "city": "São Paulo",
      "state": "São Paulo",
      "country": "Brazil"
    }
  },
  "products": [
    {
      "id": 79138421386,
      "gs1": 7908887777776,
      "name": "Something Product",
      "quantity": 3,
      "price": 10.9
    },
    {
      "id": 79138421386,
      "gs1": 7908884443339,
      "name": "Another Product",
      "quantity": 10,
      "price": 5.28
    }
  ]
}
```


### Output data

Existem 3 formas de pegar as informações das ordens via requisição REST.
1. Listando as ordens dentro de um range de data (limitado a um raio de 24 horas). Este endpoint
é focado no cliente como por exemplo um controle de estoque que irá verificar periodicamente
os pedidos para iniciar a separação dos pedidos.
2. Buscando a ordem diretamente por seu número. Este endpoint é destinado a um cliente como a
tela do cliente no site, para que ele consiga ver seu pedido por exemplo.
3. Buscando as ordens de um cliente em específico. Este endpoint tem o foco em um cliente que
precisa pegar todos os pedidos de um cliente.
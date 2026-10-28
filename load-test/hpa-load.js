// Teste de carga para demonstrar o HPA (k6: https://k6.io).
// Uso:  BASE_URL=http://IP_EXTERNO k6 run load-test/hpa-load.js
// Em outro terminal:  kubectl -n oficina get hpa -w
//
// O endpoint de health nao consulta regra de negocio; usamos o login e a
// consulta publica de OS (rotas reais, com banco) para gerar CPU de verdade.
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const DOCUMENT = __ENV.DOCUMENT || '529.982.247-25';

export const options = {
  stages: [
    { duration: '1m', target: 20 },   // aquecimento
    { duration: '4m', target: 150 },  // carga alta: o HPA deve criar replicas
    { duration: '2m', target: 150 },
    { duration: '2m', target: 0 },    // queda: replicas voltam ao minimo
  ],
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<1500'],
  },
};

export default function () {
  const login = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ email: 'carlos.souza@oficina.com', password: 'senha123' }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  check(login, { 'login 200': (r) => r.status === 200 });

  const orders = http.get(`${BASE_URL}/public/service-orders?document=${encodeURIComponent(DOCUMENT)}`);
  check(orders, { 'consulta publica 200': (r) => r.status === 200 });

  sleep(0.2);
}

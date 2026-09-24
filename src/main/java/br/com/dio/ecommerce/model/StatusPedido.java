package br.com.dio.ecommerce.model;

import java.util.Set;

/**
 * Ciclo de vida do pedido. Cada status conhece as transições permitidas:
 * <pre>
 * CRIADO ──► PAGO ──► ENVIADO ──► ENTREGUE
 *   │         │
 *   └────┬────┘
 *        ▼
 *    CANCELADO
 * </pre>
 */
public enum StatusPedido {
    CRIADO,
    PAGO,
    ENVIADO,
    ENTREGUE,
    CANCELADO;

    public boolean podeMudarPara(StatusPedido novo) {
        return proximosPermitidos().contains(novo);
    }

    public Set<StatusPedido> proximosPermitidos() {
        return switch (this) {
            case CRIADO -> Set.of(PAGO, CANCELADO);
            case PAGO -> Set.of(ENVIADO, CANCELADO);
            case ENVIADO -> Set.of(ENTREGUE);
            case ENTREGUE, CANCELADO -> Set.of();
        };
    }
}

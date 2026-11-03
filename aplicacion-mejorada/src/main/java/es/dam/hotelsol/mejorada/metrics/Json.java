package es.dam.hotelsol.mejorada.metrics;

/** Utilidades minimas de serializacion JSON (sin dependencias externas). */
final class Json {

    private Json() {
    }

    static String texto(String valor) {
        if (valor == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(valor.length() + 2);
        sb.append('"');
        for (char c : valor.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

    static String numero(Long valor) {
        return valor == null ? "null" : valor.toString();
    }
}

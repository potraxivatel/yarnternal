#version 330 core

in vec4 v_Color;
in vec2 v_Uv;
in vec2 v_Size;
in vec4 v_Radius;
in float v_Smoothness; // <--- Получаем значение

out vec4 color;

float sdRoundedBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    float alpha = 1.0;

    // v_Size > 0 — это SDF-квад (скругления). Если атрибуты формы не заданы
    // (обычный quad, линия или треугольник) — рисуем сплошной цвет:
    // smoothstep(0.0, 0.0, dist) дал бы деление на ноль и NaN.
    if (v_Size.x > 0.0 && v_Size.y > 0.0) {
        vec2 halfSize = v_Size * 0.5;
        vec2 p = (v_Uv * v_Size) - halfSize;

        // Выбор радиуса для конкретного угла
        float r = 0.0;
        if (p.x < 0.0) {
            if (p.y < 0.0) r = v_Radius.x;
            else           r = v_Radius.w;
        } else {
            if (p.y < 0.0) r = v_Radius.y;
            else           r = v_Radius.z;
        }

        float dist = sdRoundedBox(p, halfSize, r);

        // Чем больше v_Smoothness, тем шире градиент прозрачности на краю.
        float smoothness = max(v_Smoothness, 0.0001);
        alpha = 1.0 - smoothstep(0.0, smoothness, dist);
    }

    color = vec4(v_Color.rgb, v_Color.a * alpha);

    if (color.a <= 0.0) discard;
}
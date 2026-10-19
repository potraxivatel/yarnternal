#version 330 core

in vec4 v_Color;
in vec2 v_TexCoord; // Координаты атласа (например, 0.125 ... 0.25)
in vec2 v_GeoUv;    // Координаты рамки (строго 0.0 ... 1.0)
in vec2 v_Size;
in vec4 v_Radius;
in float v_Smoothness;

out vec4 color;

uniform sampler2D u_Texture;

// Стандартная SDF функция прямоугольника
float sdRoundedBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    // --- 1. ВЫЧИСЛЯЕМ ФОРМУ (ЗАКРУГЛЕНИЕ) ---
    // Используем v_GeoUv, потому что он всегда 0..1 для нашего прямоугольника.
    // Это гарантирует, что закругление будет именно по краям НАШЕГО квадрата.
    // Если size не задан (обычный texQuad без скруглений) — форма не режется.

    float shapeAlpha = 1.0;

    if (v_Size.x > 0.0 && v_Size.y > 0.0) {
        vec2 halfSize = v_Size * 0.5;
        // Превращаем 0..1 в координаты от центра (-width/2 .. +width/2)
        vec2 p = (v_GeoUv * v_Size) - halfSize;

        // Выбираем радиус для нужного угла
        float r = 0.0;
        if (p.x < 0.0) {
            if (p.y < 0.0) r = v_Radius.x; // Top-Left
            else           r = v_Radius.w; // Bottom-Left
        } else {
            if (p.y < 0.0) r = v_Radius.y; // Top-Right
            else           r = v_Radius.z; // Bottom-Right
        }

        // Считаем дистанцию и прозрачность краев
        float dist = sdRoundedBox(p, halfSize, r);
        float halfSmooth = max(v_Smoothness, 0.0001) * 0.5;
        shapeAlpha = 1.0 - smoothstep(-halfSmooth, halfSmooth, dist);
    }

    // --- 2. ПОЛУЧАЕМ ЦВЕТ КАРТИНКИ ---
    // Используем v_TexCoord, чтобы вырезать нужный кусок из атласа (голову).
    vec4 texColor = texture(u_Texture, v_TexCoord);

    // --- 3. СМЕШИВАЕМ ---
    // Цвет картинки * Тинт * Маска формы
    color = texColor * v_Color;
    color.a *= shapeAlpha;

    // Оптимизация: не рисуем полностью прозрачные пиксели
    if (color.a <= 0.01) discard;
}
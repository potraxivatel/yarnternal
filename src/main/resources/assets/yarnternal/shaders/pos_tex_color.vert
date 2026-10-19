#version 330 core

// Порядок атрибутов (Сверь с RJVertexFormats!):
layout (location = 0) in vec2 a_Pos;
layout (location = 1) in vec4 a_Color;      // Цвет
layout (location = 2) in vec2 a_TexCoord;   // <-- Координаты КАРТИНКИ (из Атласа)
layout (location = 3) in vec2 a_GeoUv;      // <-- Координаты ФОРМЫ (строго 0..1)
layout (location = 4) in vec2 a_Size;
layout (location = 5) in vec4 a_Radius;
layout (location = 6) in float a_Smoothness;

// Твой обязательный блок матриц
layout (std140) uniform MeshData {
    mat4 u_Proj;
    mat4 u_ModelView;
};

out vec4 v_Color;
out vec2 v_TexCoord;
out vec2 v_GeoUv;
out vec2 v_Size;
out vec4 v_Radius;
out float v_Smoothness;

void main() {
    gl_Position = u_Proj * u_ModelView * vec4(a_Pos, 0.0, 1.0);

    v_Color = a_Color;
    v_TexCoord = a_TexCoord; // Передаем атлас дальше
    v_GeoUv = a_GeoUv;       // Передаем форму дальше
    v_Size = a_Size;
    v_Radius = a_Radius;
    v_Smoothness = a_Smoothness;
}
// Shader name and properties
Shader "Custom/ExampleShader" {
  Properties {
    _MainTex("Main Texture", 2D) = "white" { }
    _Color("Color", Color) = (1, 1, 1, 1)
    _SliderValue("Slider Value", Range(0, 1)) = 0.5
  }

  // Subshader: Unity's way of handling multiple shader implementations
  SubShader {
    Tags { "RenderType"="Opaque" }  // Rendering tag

    CGPROGRAM
    #pragma surface surf Standard   // Surface shader type and lighting model

    // Surface shader function
    fixed4 LightingStandard(SurfaceOutputStandard s, float3 lightDir, float atten) {
      fixed4 c;
      c.rgb = s.Albedo * _LightColor0.rgb;
      c.a = s.Alpha;
      return c;
    }

    // Surface shader input structure
    struct Input {
      float2 uv_MainTex;
      float _SliderValue;
    };

    // Surface shader function
    void surf(Input IN, inout SurfaceOutputStandard o) {
      // Albedo comes from a texture tinted by color
      fixed4 c = tex2D(_MainTex, IN.uv_MainTex) * _Color;

      // Optional: Modify the color based on a slider value
      c.rgb *= IN._SliderValue;

      o.Albedo = c.rgb;
      o.Alpha = c.a;
    }
    ENDCG
  }

  // Fallback for platforms that don't support Standard shader
  Fallback "Diffuse"
}

// Vertex shader input structure
struct VertexInput {
  float3 position : POSITION;
  float3 normal : NORMAL;
  float2 texCoord : TEXCOORD0;
};

// Vertex shader output structure
struct VertexOutput {
  float4 position : POSITION;
  float3 normal : NORMAL;
  float2 texCoord : TEXCOORD0;
};

// Constant buffer for the vertex shader
cbuffer ConstantBuffer : register(b0) {
  float4x4 worldViewProjection;
}

// Vertex shader function
VertexOutput VertexMain(VertexInput input) {
  VertexOutput output;

  // Transform position to clip space
  output.position = mul(float4(input.position, 1.0), worldViewProjection);

  // Pass through normal and texture coordinates
  output.normal = input.normal;
  output.texCoord = input.texCoord;

  return output;
}

// Pixel shader input structure
struct PixelInput {
  float4 position : SV_POSITION;
  float3 normal : NORMAL;
  float2 texCoord : TEXCOORD0;
};

// Sampler state for the pixel shader
SamplerState sampler : register(s0);

// Texture resource for the pixel shader
Texture2D texture : register(t0);

// Pixel shader function
float4 PixelMain(PixelInput input) : SV_TARGET {
  // Sample texture using interpolated coordinates
  float4 texColor = texture.Sample(sampler, input.texCoord);

  // Simple lighting calculation
  float3 lightDir = normalize(float3(1, 1, -1));
  float diffuse = max(0, dot(input.normal, lightDir));

  // Combine texture color and lighting
  float4 finalColor = texColor * diffuse;

  return finalColor;
}

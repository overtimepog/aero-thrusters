from PIL import Image, ImageDraw
import os

def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))

def draw_metal_panel(draw, size, base_color, highlight, shadow, accent):
    w, h = size
    # Base fill
    draw.rectangle([0, 0, w-1, h-1], fill=base_color)
    
    # Horizontal panel lines
    for y in range(8, h, 16):
        draw.line([(0, y), (w, y)], fill=shadow, width=1)
        draw.line([(0, y+1), (w, y+1)], fill=highlight, width=1)
    
    # Vertical seams
    for x in [16, 32, 48]:
        draw.line([(x, 0), (x, h)], fill=shadow, width=1)
        draw.line([(x+1, 0), (x+1, h)], fill=highlight, width=1)
    
    # Rivets/bolts
    for y in range(12, h, 16):
        for x in [8, 24, 40, 56]:
            draw.ellipse([x-2, y-2, x+2, y+2], fill=accent)
            draw.ellipse([x-1, y-1, x+1, y+1], fill=highlight)
    
    # Center detail stripe
    stripe_y = h // 2 - 4
    draw.rectangle([4, stripe_y, w-4, stripe_y+8], fill=shadow)
    draw.rectangle([6, stripe_y+2, w-6, stripe_y+6], fill=accent)
    
    # Corner reinforcements
    for cx, cy in [(4, 4), (w-5, 4), (4, h-5), (w-5, h-5)]:
        draw.rectangle([cx-3, cy-3, cx+3, cy+3], fill=shadow)
        draw.rectangle([cx-2, cy-2, cx+2, cy+2], fill=base_color)

def draw_nozzle_face(draw, size, base_color, ring_colors, glow_color, glow_intensity):
    w, h = size
    cx, cy = w // 2, h // 2
    
    # Base
    draw.ellipse([0, 0, w-1, h-1], fill=base_color)
    
    # Concentric rings
    rings = [(28, ring_colors[0]), (22, ring_colors[1]), (16, ring_colors[2]), (10, ring_colors[3])]
    for r, color in rings:
        draw.ellipse([cx-r, cy-r, cx+r, cy+r], fill=color)
    
    # Glow center
    glow_r = 6
    for i in range(glow_r, 0, -1):
        t = i / glow_r
        color = lerp(glow_color, (255, 255, 255), 1 - t)
        draw.ellipse([cx-i, cy-i, cx+i, cy+i], fill=color)
    
    # Bolt holes around rings
    import math
    for r in [26, 18, 12]:
        for angle in range(0, 360, 45):
            rad = math.radians(angle)
            bx = cx + int(r * math.cos(rad))
            by = cy + int(r * math.sin(rad))
            draw.ellipse([bx-2, by-2, bx+2, by+2], fill=(40, 40, 40))
            draw.ellipse([bx-1, by-1, bx+1, by+1], fill=(80, 80, 80))

def draw_nozzle_side(draw, size, base_color, band_colors):
    w, h = size
    draw.rectangle([0, 0, w-1, h-1], fill=base_color)
    
    # Horizontal bands
    for i, y in enumerate(range(2, h, 6)):
        color = band_colors[i % len(band_colors)]
        draw.rectangle([0, y, w, y+3], fill=color)
        draw.line([(0, y), (w, y)], fill=(255, 255, 255), width=1)
    
    # Vertical seams
    for x in [w//4, w//2, 3*w//4]:
        draw.line([(x, 0), (x, h)], fill=(30, 30, 30), width=1)

def generate_tier(name, side_base, side_highlight, side_shadow, side_accent,
                  front_base, front_rings, front_glow, glow_intensity,
                  nozzle_base, nozzle_bands):
    out_dir = "src/main/resources/assets/aerothrusters/textures/block"
    os.makedirs(out_dir, exist_ok=True)
    
    # Side texture (64x64)
    img = Image.new("RGB", (64, 64), side_base)
    draw = ImageDraw.Draw(img)
    draw_metal_panel(draw, (64, 64), side_base, side_highlight, side_shadow, side_accent)
    img.save(f"{out_dir}/{name}_side.png")
    
    # Front texture (64x64)
    img = Image.new("RGB", (64, 64), front_base)
    draw = ImageDraw.Draw(img)
    draw_nozzle_face(draw, (64, 64), front_base, front_rings, front_glow, glow_intensity)
    img.save(f"{out_dir}/{name}_front.png")
    
    # Nozzle texture (32x32)
    img = Image.new("RGB", (32, 32), nozzle_base)
    draw = ImageDraw.Draw(img)
    draw_nozzle_side(draw, (32, 32), nozzle_base, nozzle_bands)
    img.save(f"{out_dir}/{name}_nozzle.png")
    
    print(f"Generated textures for {name}")

# Basic tier - Rust/Orange industrial
generate_tier(
    "thruster",
    side_base=(75, 65, 55), side_highlight=(100, 90, 80), side_shadow=(50, 40, 35), side_accent=(120, 80, 50),
    front_base=(80, 70, 60), front_rings=[(100, 85, 70), (120, 100, 80), (140, 110, 90), (160, 120, 100)],
    front_glow=(255, 140, 40), glow_intensity=1.0,
    nozzle_base=(90, 80, 70), nozzle_bands=[(110, 95, 80), (130, 110, 90), (100, 85, 70)]
)

# Advanced tier - Blue/Teal high-tech
generate_tier(
    "advanced_thruster",
    side_base=(55, 65, 75), side_highlight=(80, 95, 110), side_shadow=(35, 40, 50), side_accent=(50, 120, 140),
    front_base=(60, 70, 85), front_rings=[(70, 90, 110), (80, 110, 130), (90, 130, 150), (100, 150, 170)],
    front_glow=(80, 200, 255), glow_intensity=1.2,
    nozzle_base=(65, 75, 90), nozzle_bands=[(80, 100, 120), (100, 130, 150), (70, 90, 110)]
)

# Superior tier - Purple/White ultra-tech
generate_tier(
    "superior_thruster",
    side_base=(50, 45, 60), side_highlight=(80, 75, 95), side_shadow=(30, 25, 40), side_accent=(140, 50, 160),
    front_base=(55, 50, 70), front_rings=[(75, 65, 90), (95, 80, 110), (115, 95, 130), (135, 110, 150)],
    front_glow=(220, 120, 255), glow_intensity=1.5,
    nozzle_base=(60, 55, 75), nozzle_bands=[(85, 75, 100), (110, 95, 130), (75, 65, 90)]
)

print("All textures generated!")

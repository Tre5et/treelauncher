package net.treset.treelauncher.backend.util

import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage

class Images {
    companion object {
        fun scale(img: BufferedImage, factor: Int): BufferedImage {
            val w = img.width
            val h = img.height
            val at = AffineTransform()
            at.scale(factor.toDouble(), factor.toDouble())
            val scaleOp = AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR)
            return scaleOp.filter(img, BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB))
        }

        fun overlay(base: BufferedImage, top: BufferedImage): BufferedImage {
            val w = base.width.coerceAtLeast(top.width)
            val h = base.height.coerceAtLeast(top.height)
            val combined = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)

            val g = combined.graphics;
            g.drawImage(base, 0, 0, null)
            g.drawImage(top, 0, 0, null)

            return combined
        }

        fun crop(img: BufferedImage, x: Int, y: Int, width: Int, height: Int): BufferedImage {
            return img.getSubimage(x, y, width, height)
        }
    }
}

# Coloring Book Business - Krea.ai MCP Setup

## Overview
Portfolio and marketing site for the Coloring Book Business powered by Krea.ai MCP. This site showcases generated coloring book samples and provides direct links to Amazon KDP listings.

## MCP Configuration

### Krea.ai MCP Server
- **API Endpoint**: https://api.krea.ai/mcp
- **Configuration**: `.claude/mcp-servers.json`
- **Capabilities**: AI image generation, design variation, portfolio generation

## Site Structure
```
.
├── coloring-books/           # Portfolio of coloring books
├── amazon-kpd-links/         # Amazon listing links
├── design-samples/           # Sample pages from books
├── mcp-showcase/             # MCP generation examples
└── business-info/            # Business documentation
```

## Portfolio Showcase

### Featured Designs
- Adult Mandalas & Patterns
- Children's Animals Collection
- Nature & Botanical Designs
- Seasonal & Holiday Themes

## Amazon Integration

### Direct Links Format
Each coloring book has:
- Portfolio preview with sample pages
- Direct Amazon KDP link
- Customer reviews
- Available formats (paperback, hardcover)

## Krea.ai MCP Usage

### Portfolio Generation
Generate website banners and showcase images from Krea.ai designs:
```
Krea.ai Design → Web Optimization → Portfolio Display
```

### Dynamic Content
Update portfolio automatically as new designs are published to Amazon KDP.

## Setup Instructions

1. Deploy to GitHub Pages: `npm run build && npm run deploy`
2. Configure Amazon affiliate links (optional)
3. Set up email subscription for new releases
4. Monitor analytics and sales

## Business Metrics

Track:
- Portfolio views
- Amazon KDP click-throughs
- Sales per design category
- Customer feedback and ratings

## Resources
- GitHub Pages deployment: See `.github/workflows/`
- Krea.ai MCP: https://api.krea.ai/mcp
- Amazon KDP: https://kdp.amazon.com

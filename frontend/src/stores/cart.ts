import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface CartLine {
  skuId: number
  productName: string
  unitPrice: number
  quantity: number
}

export const useCartStore = defineStore('cart', () => {
  const lines = ref<CartLine[]>([])

  function add(line: CartLine) {
    const existing = lines.value.find((l) => l.skuId === line.skuId)
    if (existing) {
      existing.quantity += line.quantity
    } else {
      lines.value.push({ ...line })
    }
  }

  function clear() {
    lines.value = []
  }

  return { lines, add, clear }
})

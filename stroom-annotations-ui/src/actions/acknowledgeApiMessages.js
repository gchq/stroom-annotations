export const ACKNOWLEDGE_ERROR = 'ACKNOWLEDGE_ERROR'

export const acknowledgeError = (id) => ({
    type: ACKNOWLEDGE_ERROR,
    id
})

export const ACKNOWLEDGE_SNACKBAR = 'ACKNOWLEDGE_SNACKBAR'

export const acknowledgeSnackbar = (id) => ({
    type: ACKNOWLEDGE_SNACKBAR,
    id
})

export const GENERIC_ERROR = 'GENERIC_ERROR'

export const genericError = (message) => ({
    type: GENERIC_ERROR,
    message
})

export const GENERIC_SNACKBAR = 'GENERIC_SNACKBAR'

export const genericSnackbar = (message) => ({
    type: GENERIC_SNACKBAR,
    message
})
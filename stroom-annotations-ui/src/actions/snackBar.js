
export const ACKNOWLEDGE_SNACKBAR = 'ACKNOWLEDGE_SNACKBAR'

export const acknowledgeSnackbar = (id) => ({
    type: ACKNOWLEDGE_SNACKBAR,
    id
})

export const SEND_TO_SNACKBAR = 'SEND_TO_SNACKBAR'

export const sendToSnackbar = (message) => ({
    type: SEND_TO_SNACKBAR,
    message
})
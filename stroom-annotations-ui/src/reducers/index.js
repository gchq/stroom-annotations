import { combineReducers } from 'redux'

import singleAnnotation from './singleAnnotation'
import annotationHistory from './annotationHistory'
import manageAnnotations from './manageAnnotations'
import statusValues from './statusValues'
import snackbarMessages from './snackbarMessages'
import errorMessages from './errorMessages'

const annotationsApp = combineReducers({
    singleAnnotation,
    annotationHistory,
    manageAnnotations,
    statusValues,
    snackbarMessages,
    errorMessages
})

export default annotationsApp

import { combineReducers } from 'redux'
import annotation from './annotation'
import annotations from './annotations'
import statusValues from './statusValues'

const annotationsApp = combineReducers({
  annotation, annotations, statusValues
})

export default annotationsApp

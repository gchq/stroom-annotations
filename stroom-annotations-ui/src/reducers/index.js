import { combineReducers } from 'redux'
import annotation from './annotation'
import statusValues from './statusValues'

const annotationsApp = combineReducers({
  annotation, statusValues
})

export default annotationsApp

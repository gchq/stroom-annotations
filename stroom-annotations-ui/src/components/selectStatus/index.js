import { connect } from 'react-redux'

import SelectStatus from './selectStatus'

export default connect(
   (state) => ({
      statusValues: state.statusValues
   }),
   null
 )(SelectStatus)
